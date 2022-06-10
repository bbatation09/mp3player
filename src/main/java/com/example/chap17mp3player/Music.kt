package com.example.chap17mp3player

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import java.io.IOException
import java.io.Serializable

class Music(id: String, title: String?, artist: String?, albumID: String?, duration: Long?, favorite: Int?): Serializable {

    //멤버변수
    var id: String = ""
    var title: String? = ""
    var artist: String? = ""
    var albumID: String? = ""
    var duration: Long? = 0
    var favorite: Int? = 0

    //생성자 멤버변수 초기화 기능
    init {
        this.id = id
        this.title = title
        this.artist = artist
        this.albumID = albumID
        this.duration = duration
        this.favorite = favorite
    }

    //음악정보를 가져오기 위한 경로 uri 얻기
    fun getMusicUri(): Uri {
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    }

    //Content Resovler를 이용해서 외장메모리에서 albumID에 따른 앨범이미지정보를 가져오기 위해 경로 uri 얻기
    fun getAlbumUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/" + albumID)
    }

    //가져오고 싶은 앨범이미지를 내가 원하는 사이즈로 비트맵을 만들어 리턴값으로 제공함
    fun getAlbumImage(context: Context, albumImageSize: Int): Bitmap? {
        val contentResovler: ContentResolver = context.getContentResolver()
        //앨범 경로
        val uri = getAlbumUri()
        //앨범에 대한 정보를 저장하기 위함
        val options = BitmapFactory.Options()

        if (uri != null) {
            var parcelFileDescriptor: ParcelFileDescriptor? = null

            try {
                //외부에 있는 이미지파일을 가져오기 위한 stream
                parcelFileDescriptor = contentResovler.openFileDescriptor(uri, "r")

                var bitmap = BitmapFactory.decodeFileDescriptor(
                    parcelFileDescriptor!!.fileDescriptor,
                    null,
                    options)

                //원본이미지의 사이즈가 내가 원하는 사이즈와 맞지 않은 경우 비트맵을 가져와서 내가 원하는 사이즈대로 가져온다
                if (bitmap != null) {

                    if (options.outHeight != albumImageSize || options.outWidth != albumImageSize) {

                        //내가 원하는 사이즈로 생성시킨 비트맵: tempBitmap
                        val tempBitmap = Bitmap.createScaledBitmap(bitmap, albumImageSize, albumImageSize, true)
                        //기존의 비트맵을 버리고 내가 만든 비트맵을 그 자리에 배치시킴
                        bitmap.recycle()
                        //내가 원하는 사이즈로 저장함
                        bitmap = tempBitmap

                    }
                }

                return bitmap

            }catch (e: Exception){
                Log.d("kim","getAlbumImage() ${e.toString()}")
            }finally {
                try {
                    parcelFileDescriptor?.close()
                }catch (e: IOException){
                    Log.d("kim","parcelFileDescriptor?.close() ${e.toString()}")
                }

            }

        }//end of if(uri != null)

        return null

    }//end of getAlbumImage

}

