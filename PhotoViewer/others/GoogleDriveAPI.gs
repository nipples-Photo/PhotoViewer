//プロジェクトのプロパティにGOOGLE_DRIVE_FOLDER_IDとLINE_ACCESS_TOKENを設定する。
//値はそれぞれからとってきたものを使う。

// プロパティ取得
var PROPERTIES = PropertiesService.getScriptProperties();//ファイル > プロジェクトのプロパティから設定した環境変数的なもの

//Google Driveの画像を保存するフォルダの設定

var GOOGLE_DRIVE_FOLDER_ID = PROPERTIES.getProperty('GOOGLE_DRIVE_FOLDER_ID')

//LINE・DMMの設定をプロジェクトのプロパティから取得
var LINE_ACCESS_TOKEN = PROPERTIES.getProperty('LINE_ACCESS_TOKEN')
var LINE_END_POINT = "https://api.line.me/v2/bot/message/reply"

function doGet(e){
  var html = '';
  html += '<h1>This is test</h1>';
  html += '<p>access ok (get)</p>';
  return HtmlService.createHtmlOutput(html);
}

//LINEからPOSTリクエストを受けたときに起動する
function doPost(e){
  
  Logger.log("処理開始")
  

    /*
     * debug用の処理です
     * imageUrlに、任意のAV女優の画像を挿入しています。
    */
    var imageEndPoint = "http://eropalace21.com/wordpress/wp-content/uploads/2016/01/sakuramana_thumb.jpg" //検証用の画像

    /*
     * Lineからメッセージが送られたときの処理です
     * LineのmessageIdを取得し、そこからバイナリ形式の画像データを取得します
    */

    //messageIdから、Line上に存在するバイナリ形式の画像URLを取得します
    try {
      var json = JSON.parse(e.postData.contents);
      reply_token= json.events[0].replyToken;
      var messageId = json.events[0].message.id;
      imageEndPoint = "https://api.line.me/v2/bot/message/" + messageId + "/content"
      
      imageBlob = getImageBlobByImageUrl(imageEndPoint);
      imageUrl = saveImageBlobAsPng(imageBlob)
    } catch (e) {
      Logger.log("[ERROE]eventArgsのParseに失敗しました。")
      Logger.log(e)
    }

}

 function getImageBlobByImageUrl(url){

  /* LineのメッセージIDから、送られた画像をBlob形式で取得します、
   * @params
    - url{string}: 取得したい画像のURLです
   * @return
   * - imageBlob<string>: Blob形式で取得した画像ファイル
  */

  try {
    var res = UrlFetchApp.fetch(url, {
      'headers': {
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Bearer ' + LINE_ACCESS_TOKEN,
      },
      'method': 'get'
    });

    var dateString = Utilities.formatDate(new Date(),"JST","yyyyMMdd_hhmmss");
    var imageBlob = res.getBlob().getAs("image/png").setName(dateString + ".png")
    Logger.log("imageBlobの取得に成功しました")
    Logger.log("ContentType:" + imageBlob.getContentType())
    Logger.log("Name: " + imageBlob.getName())
    //Logger.log("")
    return imageBlob;
    
  } catch (e) {
    Logger.log("[ERROE]Google Driveに画像を保存できませんでした")
    Logger.log(e)
  }
}

function saveImageBlobAsPng(imageBlob){

  /*
    @params
      - imageBlob
    @void
      - Google Drive上の指定されたフォルダに画像を保存します
  */

  try{
    var folder = DriveApp.getFolderById(GOOGLE_DRIVE_FOLDER_ID);
    var file = folder.createFile(imageBlob);
    Logger.log("[INFO] Google Driveに以下のURLに画像が保存されました: " + folder.getUrl())
    Logger.log("file.getUrl():" + file.getUrl())
    return file.getUrl()
  } catch (e) {
    Logger.log("[ERROE]Google Driveに画像を保存できませんでした")
    Logger.log(e)
  }

}