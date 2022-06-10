package com.example.chap17mp3player

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chap17mp3player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

//    companion object{
//        val DB_NAME = "musicTBL"
//        val VERSION = 1
//    }

    lateinit var binding: ActivityMainBinding

    //1. 승인 받아야할 항목
    //나중에 항목을 여러가지 추가하면 arrayOf를 쓰게됨
    val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val REQUEST_READ = 200

    //2. DB 객체화
    val dbHelper : DBHelper by lazy {
        DBHelper(this,"musicTBL",1)
//        DBHelper(this,"DB_NAME",VERSION)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //승인: 음악파일을 가져옴 / 승인거부: 재요청한다
        if (isPermitted() == true) {
            //외부에서 파일을 Content Resolver로 가져와서 뮤터블 리스트에 저장함
            //중복되는 경우는 제외시킴
            startProcess()
        } else {
            //요청이 승인되면 콜백함수로 승인결과값을 알려준다
            ActivityCompat.requestPermissions(this, permission, REQUEST_READ)
        }

    }


    //승인 결과에 대한 콜백함수
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startProcess()
            } else {
                Toast.makeText(this, "권한 요청 미승인시 앱 실행 불가능", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    //외부파일 읽기 승인 요청
    fun isPermitted(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                permission[0]
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        } else {
            return true
        }

    }

    //외부파일로부터 모든 음악정보를 가져오는 함수(여기서 DB설정함)
    private fun startProcess() {
     //DB에 저장 (id를 primary key로 설정해서 중복을 방어하자)

        //1. musicTBL에 자료가 있으면 리사이클러뷰를 통해 보여준다
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        musicList = dbHelper.selectMusicAll()

        //1-1. musicTBL에 자료가 없으면
        if(musicList == null || musicList.size <= 0){
            //getMusicList()를 통해 가져와서
            musicList = getMusicList()
            //musicTBL에 모두 저장시킴
            for(i in 0..(musicList!!.size - 1)){
                val music = musicList.get(i)
                if(dbHelper.insertMusic(music) == false){
                    Log.d("kim","삽입 오류: ${music.toString()}")
                }
            }
            Log.d("kim","테이블에 자료가 없어서 getMusicList를 통해 가져옴")
        }else{
            Log.d("kim","테이블에 자료가 있어서 내용을 가져와서 보여줌")
        }

        //3. 어댑터 생성 후 MutableList 제공
        val musicRecyclerAdapter = MusicRecyclerAdapter(this,musicList)
        binding.recyclerView.adapter = musicRecyclerAdapter

        //4. 화면에 출력
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        //5. 꾸미기는 자유

    }

    private fun getMusicList(): MutableList<Music>? {
        //1. 외부파일에 있는 음악정보 주소 가져오기
        val listUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        //2. 요청하고자하는 음원정보의 행들
        val proj = arrayOf(
            MediaStore.Audio.Media._ID,       //음악 ID
            MediaStore.Audio.Media.TITLE,     //음악 타이틀
            MediaStore.Audio.Media.ARTIST,    //가수명
            MediaStore.Audio.Media.ALBUM_ID,  //음악 이미지
            MediaStore.Audio.Media.DURATION   //음악시간
        )

        //3. content resolver에 uri, 요청할 음원정보에 대한 행을 요구하고 결과값을 cursor로 반환받음
        val cursor = contentResolver.query(listUri, proj, null, null, null)

        //music의 기능: mp3정보 5가지 기억, mp3 파일경로, mp3 이미지경로, 이미지를 내가 원하는 사이즈의 비트맵으로 바꿈
        val musicList = mutableListOf<Music>()

        while (cursor?.moveToNext() == true) {
            val id = cursor.getString(0)
            //타이틀,가수명에 '가 있으면 공백으로 바꿔준다
            val title = cursor.getString(1).replace("'","")
            val artist = cursor.getString(2).replace("'","")
            val albumID = cursor.getString(3)
            val duration = cursor.getLong(4)

            val music = Music(id, title, artist, albumID, duration)
            musicList?.add(music)
        }
        //커서도 닫아줘야 한다
        cursor?.close()
        return musicList
    }
}