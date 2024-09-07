//@formatter:off
/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.2.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package me.him188.ani.app.torrent.anitorrent.binding;

public class peer_info_t {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected peer_info_t(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(peer_info_t obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(peer_info_t obj) {
    long ptr = 0;
    if (obj != null) {
      if (!obj.swigCMemOwn)
        throw new RuntimeException("Cannot release ownership as memory is not owned");
      ptr = obj.swigCPtr;
      obj.swigCMemOwn = false;
      obj.delete();
    }
    return ptr;
  }

  @SuppressWarnings({"deprecation", "removal"})
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        anitorrentJNI.delete_peer_info_t(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setPeer_id(String value) {
    anitorrentJNI.peer_info_t_peer_id_set(swigCPtr, this, value);
  }

  public String getPeer_id() {
    return anitorrentJNI.peer_info_t_peer_id_get(swigCPtr, this);
  }

  public void setClient(String value) {
    anitorrentJNI.peer_info_t_client_set(swigCPtr, this, value);
  }

  public String getClient() {
    return anitorrentJNI.peer_info_t_client_get(swigCPtr, this);
  }

  public void setIp_addr(String value) {
    anitorrentJNI.peer_info_t_ip_addr_set(swigCPtr, this, value);
  }

  public String getIp_addr() {
    return anitorrentJNI.peer_info_t_ip_addr_get(swigCPtr, this);
  }

  public void setIp_port(int value) {
    anitorrentJNI.peer_info_t_ip_port_set(swigCPtr, this, value);
  }

  public int getIp_port() {
    return anitorrentJNI.peer_info_t_ip_port_get(swigCPtr, this);
  }

  public peer_info_t() {
    this(anitorrentJNI.new_peer_info_t(), true);
  }

}

//@formatter:on