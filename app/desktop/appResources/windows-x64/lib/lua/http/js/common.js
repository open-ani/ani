var intv = 0;
var ccmd = "";
var video_types = [
	"asf", "avi", "bik", "bin", "divx", "drc", "dv", "f4v", "flv", "gxf", "iso",
	"m1v", "m2v", "m2t", "m2ts", "m4v", "mkv", "mov",
	"mp2", "mp4", "mpeg", "mpeg1",
	"mpeg2", "mpeg4", "mpg", "mts", "mtv", "mxf", "mxg", "nuv",
	"ogg", "ogm", "ogv", "ogx", "ps",
	"rec", "rm", "rmvb", "rpl", "thp", "ts", "txd", "vob", "wmv", "xesc"];
var audio_types = [
	"3ga", "a52", "aac", "ac3", "ape", "awb", "dts", "flac", "it",
	"m4a", "m4p", "mka", "mlp", "mod", "mp1", "mp2", "mp3",
	"oga", "ogg", "oma", "s3m", "spx", "thd", "tta",
	"wav", "wma", "wv", "xm"
];
var playlist_types = [
	"asx", "b4s", "cue", "ifo", "m3u", "m3u8", "pls", "ram", "rar",
	"sdp", "vlc", "xspf", "zip", "conf"
];

var stream_server = window.location.hostname;

function format_time(s) {
	var hours = Math.floor(s / 3600);
	var minutes = Math.floor((s / 60) % 60);
	var seconds = Math.floor(s % 60);
	hours = hours < 10 ? "0" + hours : hours;
	minutes = minutes < 10 ? "0" + minutes : minutes;
	seconds = seconds < 10 ? "0" + seconds : seconds;
	return hours + ":" + minutes + ":" + seconds;
}

function toFloat(text) {
	return parseFloat(text.replace(',', '.'));
}

function setIntv() {
	if (intv > 0) {
		intv++;
		setTimeout(setIntv, 500);
	} else {
		intv = 0;
	}
	if (intv > 5) {
		var nt = 0;
		switch (ccmd) {
			case 'prev':
				nt = Math.max(0, $('#seekSlider').slider('value') - 10);
				break;
			case 'next':
				nt = Math.max(0, $('#seekSlider').slider('value') + 10);
				break;
		}
		switch (current_que) {
			case 'main':
				sendCommand({
					'command': 'seek',
					'val': Math.round((nt / 100) * $('#seekSlider').attr('totalLength')),
					plreload: false
				});
				break;
			case 'stream':
				sendVLMCmd('control Current seek ' + nt);
				break;
		}
	}
}

function isMobile() {
	var a = navigator.userAgent || navigator.vendor || window.opera;
	if (/android|avantgo|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(a) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|e\-|e\/|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|xda(\-|2|g)|yas\-|your|zeto|zte\-/i.test(a.substr(0, 4))) {
		return true;
	}
	return false;
}

function createElementLi(name, type, dir, ext) {
	var icon = "Other-48.png";
	if (type == "dir" && name == '..')
		icon = "Back-48.png";
	else if (type == 'dir')
		icon = "Folder-48.png";
	else if ($.inArray(ext, video_types) != -1)
		icon = "Video-48.png";
	else if ($.inArray(ext, audio_types) != -1)
		icon = "Audio-48.png";
	else if ($.inArray(ext, playlist_types) != -1)
			// TODO: Playlist-48.png
		icon = "Other-48.png";
	var open = type == "dir" ? "opendir='" + dir + "'" : (type == "file" ? "openfile='" + dir + "'" : "opendev='" + dir + "'");
	var str = "<li class='system_icon ui-widget-content' " + open + " ><img src='images/" + icon + "' width='48px' height='48px' title='" + name + "' alt='" + name + "' style='border: none;background:none;'/><div style='font-size:10px;border:none;background:none;'>" + name + "</div></li>";
	return str;
}
