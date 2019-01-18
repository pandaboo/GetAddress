
function common_movePage(url, jsonData) {

        if(jsonData != null) {
            window.android.movePage(url, JSON.stringify(jsonData));
        } else {
            window.android.movePage(url, null);
        }
}
