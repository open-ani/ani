// Copyright (c) 2022 Klemens D. Morgenstern
// Copyright (c) 2022 Samuel Venable
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_PROCESS_V2_IMPL_CWD_IPP
#define BOOST_PROCESS_V2_IMPL_CWD_IPP

#include <boost/process/v2/detail/config.hpp>
#include <boost/process/v2/detail/last_error.hpp>
#include <boost/process/v2/detail/throw_error.hpp>
#include <boost/process/v2/ext/detail/proc_info.hpp>
#include <boost/process/v2/ext/cwd.hpp>

#include <string>

#if defined(BOOST_PROCESS_V2_WINDOWS)
#include <windows.h>
#else
#include <climits>
#endif

#if (defined(__APPLE__) && defined(__MACH__))
#include <sys/proc_info.h>
#include <libproc.h>
#endif

#if (defined(BOOST_PROCESS_V2_WINDOWS) || defined(__linux__) || defined(__ANDROID__) || defined(__sun))
#include <cstdlib>
#endif

#if defined(__FreeBSD__)
#include <sys/socket.h>
#include <sys/sysctl.h>
#include <sys/param.h>
#include <sys/queue.h>
#include <sys/user.h>
#include <libprocstat.h>
#endif

#if (defined(__NetBSD__) || defined(__OpenBSD__))
#include <sys/types.h>
#include <sys/sysctl.h>
#endif

#if defined(__DragonFly__)
#include <cstring>
#include <cstdio>
#endif

#ifdef BOOST_PROCESS_USE_STD_FS
namespace filesystem = std::filesystem;
#else
namespace filesystem = boost::filesystem;
#endif

BOOST_PROCESS_V2_BEGIN_NAMESPACE

namespace ext {

#if defined(BOOST_PROCESS_V2_WINDOWS)

filesystem::path cwd(HANDLE proc, boost::system::error_code & ec)
{
    auto buffer = boost::process::v2::detail::ext::cwd_cmd_from_proc(proc, 1/*=MEMCWD*/, ec);
    if (!buffer.empty())
      return filesystem::canonical(buffer, ec);
    else 
        BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
    return "";
}

filesystem::path cwd(boost::process::v2::pid_type pid, boost::system::error_code & ec)
{
    struct del
    {
        void operator()(HANDLE h)
        {
            ::CloseHandle(h);
        };
    };
    std::unique_ptr<void, del> proc{detail::ext::open_process_with_debug_privilege(pid, ec)};
    if (proc == nullptr)
        BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
    else
        return cwd(proc.get(), ec);
    return {};
}

filesystem::path cwd(HANDLE proc)
{
    boost::system::error_code ec;
    auto res = cwd(proc, ec);
    if (ec)
        detail::throw_error(ec, "cwd");
    return res;
}

#elif (defined(__APPLE__) && defined(__MACH__))

filesystem::path cwd(boost::process::v2::pid_type pid, boost::system::error_code & ec)
{
    proc_vnodepathinfo vpi;
    if (proc_pidinfo(pid, PROC_PIDVNODEPATHINFO, 0, &vpi, sizeof(vpi)) > 0)
        return filesystem::canonical(vpi.pvi_cdir.vip_path, ec);
    else
      BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
    return "";
}

#elif (defined(__linux__) || defined(__ANDROID__) || defined(__sun))

filesystem::path cwd(boost::process::v2::pid_type pid, boost::system::error_code & ec)
{
#if (defined(__linux__) || defined(__ANDROID__))
    return filesystem::canonical(
            filesystem::path("/proc") / std::to_string(pid) / "cwd", ec
            );
#elif defined(__sun)
    return fileystem::canonical(
            filesystem::path("/proc") / std::to_string(pid) / "path/cwd", ec
            );
#endif
}

#elif defined(__FreeBSD__)

filesystem::path cwd(boost::process::v2::pid_type pid, boost::system::error_code & ec) 
{
    filesystem::path path;
    unsigned cntp = 0;
    procstat *proc_stat = procstat_open_sysctl();
    if (proc_stat) {
        kinfo_proc *proc_info = procstat_getprocs(proc_stat, KERN_PROC_PID, pid, &cntp);
        if (proc_info) {
            filestat_list *head = procstat_getfiles(proc_stat, proc_info, 0);
            if (head) {
                filestat *fst = nullptr;
                STAILQ_FOREACH(fst, head, next) {
                    if (fst->fs_uflags & PS_FST_UFLAG_CDIR) 
                        path = filesystem::canonical(fst->fs_path, ec);
                }
                procstat_freefiles(proc_stat, head);
            }
            else
                BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
            procstat_freeprocs(proc_stat, proc_info);
        }
        else
            BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
        procstat_close(proc_stat);
    }
    else
         BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
    return path;
}

#elif defined(__DragonFly__)

filesystem::path cwd(boost::process::v2::pid_type pid, boost::system::error_code & ec) 
{
    filesystem::path path;
    /* Probably the hackiest thing ever we are doing here, because the "official" API is broken OS-level. */
    FILE *fp = popen(("pos=`ans=\\`/usr/bin/fstat -w -p " + std::to_string(pid) + " | /usr/bin/sed -n 1p\\`; " +
        "/usr/bin/awk -v ans=\"$ans\" 'BEGIN{print index(ans, \"INUM\")}'`; str=`/usr/bin/fstat -w -p " + 
        std::to_string(pid) + " | /usr/bin/sed -n 3p`; /usr/bin/awk -v str=\"$str\" -v pos=\"$pos\" " +
        "'BEGIN{print substr(str, 0, pos + 4)}' | /usr/bin/awk 'NF{NF--};1 {$1=$2=$3=$4=\"\"; print" +
        " substr($0, 5)'}").c_str(), "r");
    if (fp) 
    {
        char buffer[PATH_MAX];
        if (fgets(buffer, sizeof(buffer), fp)) 
        {
            std::string str = buffer;
            std::size_t pos = str.find("\n", strlen(buffer) - 1);
            if (pos != std::string::npos) 
            {
                str.replace(pos, 1, "");
            }
            path = filesystem::canonical(str.c_str(), ec);
        }
        else
            BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
        if (pclose(fp) == -1)
            BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
    }
    else
        BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
    return path;
}

#elif (defined(__NetBSD__) || defined(__OpenBSD__))

filesystem::path cwd(boost::process::v2::pid_type pid, boost::system::error_code & ec)
{
    std::string path;
#if defined(__NetBSD__)
    int mib[4] = {CTL_KERN, KERN_PROC_ARGS, pid, KERN_PROC_CWD};
    const std::size_t sz = 4;
#elif defined(__OpenBSD__)
    int mib[3] = {CTL_KERN, KERN_PROC_CWD, pid};
    const std::size_t sz = 3;
#endif
    std::size_t len = 0;
    if (sysctl(mib, sz, nullptr, &len, nullptr, 0) == 0) 
    {
        std::string strbuff;
        strbuff.resize(len);
        if (sysctl(mib, 4, &strbuff[0], &len, nullptr, 0) == 0)
        {
            filesystem::canonical(strbuff, ec);
        }
        else
            BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
    }

    return "";
}

#else
#error "Platform not supported"
#endif

filesystem::path cwd(boost::process::v2::pid_type pid)
{
    boost::system::error_code ec;
    auto res = cwd(pid, ec);
    if (ec)
        detail::throw_error(ec, "cwd");
    return res;
}

} // namespace ext

BOOST_PROCESS_V2_END_NAMESPACE

#endif // BOOST_PROCESS_V2_IMPL_CWD_IPP

