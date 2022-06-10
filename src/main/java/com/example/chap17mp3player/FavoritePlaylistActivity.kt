package com.example.chap17mp3player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chap17mp3player.databinding.ActivityFavoritePlaylistBinding


class FavoritePlaylistActivity : AppCompatActivity() {
    lateinit var binding : ActivityFavoritePlaylistBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritePlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //인텐트로 실행한 액티비티를 끝냄 (=다시 플레이화면으로 돌아감)
        binding.ivGoBack.setOnClickListener {
            finish()
        }

        //DB 객체화
        val dbHelper : DBHelper by lazy {
            DBHelper(this,"musicTBL",1)
        }

        //좋아요 누른 음악(favorite: 100)리스트 받기
        var musicList : MutableList<Music> = dbHelper.getFavorite()

        //3. 어댑터 생성 후 MutableList 제공
        binding.recyclerView.adapter = FavoritePlaylistAdapter(this,musicList)

        //4. 화면에 출력
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 구분선
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager(this).orientation)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)



    }


}
