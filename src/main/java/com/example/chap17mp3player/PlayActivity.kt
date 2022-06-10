package com.example.chap17mp3player

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.chap17mp3player.databinding.ActivityPlayBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class PlayActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlayBinding

    //1. 뮤직플레이어 변수
    private var mediaPlayer: MediaPlayer? = null

    //2. 리스트에서 클릭해서 재생되고 있는 음악에 관한 정보 객체 변수
    private var music: Music? = null

    //2-1. 그 음악의 위치값
    private var position : Int = 0

    //3. 앨범이미지 사이즈
    private var ALBUM_IMAGE_SIZE = 150

    //4. coroutine scope launch
    private var playerJob: Job? = null

    //5. Music 데이터를 받는 리스트
    private var musicList : MutableList<Music>? = mutableListOf()

    //6.
    private var fromActivity : String = ""

    //DB 객체화
    val dbHelper : DBHelper by lazy {
        DBHelper(this,"musicTBL",1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        music = intent.getSerializableExtra("music") as Music
        position = intent.getIntExtra("position",position)
        fromActivity = intent.getStringExtra("from").toString()

        //플레이액티비티로 보내는 인텐트를 통해 musicList가 전체 음악 목록 저장 vs 좋아요 누른 음악만 저장 -> 결정가능
        if(fromActivity == "main"){
            musicList = dbHelper.selectMusicAll()
        }else if(fromActivity == "favorite"){
            musicList = dbHelper.getFavorite()
        }

        when(music?.favorite){
            0 -> binding.ivFavorite.setImageResource(R.drawable.icon_empty_heart)
            100 -> binding.ivFavorite.setImageResource(R.drawable.icon_filled_heart)
        }

        //클릭한 음악의 재생화면으로 넘어왔을 때부터 음악 자동재생되기까지의 과정
        prepareToPlay(music)
        mediaPlayer?.start()
        binding.ivPlay.setImageResource(R.drawable.icon_btnpause)
        syncWorking()

    }

    //좋아요 누른 음악목록에서 음악을 선택하여 재생화면을 띄웠을 때
//    override fun onRestart() {
//        musicStop()
//        super.onRestart()
//    }

    override fun onStop() {
        super.onStop()
        musicStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicStop()
    }

    //뒤로가기 버튼 눌렀을 때 음악 정지
    override fun onBackPressed() {
        super.onBackPressed()
        musicStop()
    }


    fun onClickView(view: View?){
        when(view?.id){

            R.id.ivPrev -> {
                musicStop()
                //db에 있는 음악 전부 다 가져옴

                //music은 클릭했을 때 나오는 음악에 대한 정보만을 제공함
                //나는 리스트에서 클릭해서 이동하는게 아니라 플레이화면에서 이동하는것이기 때문에 처음 클릭한 음악을 중심으로 이동해야함 -1 -2...

                //현재 재생중인 음악이 처음곡(위치값: 0)이 아닐 때
                if(position != 0) {
                    position--
                    music = musicList!!.get(position)
//                    music = musicList[position]
                } else{
                    //현재 재생중인 음악이 처음곡(위치값: 0)일 때는 이전곡이 마지막곡(위치값: 음악목록-1 )이 된다
                    position = musicList!!.size - 1
                    music = musicList!!.get(position)
//                    music = musicList[position]
                }

                prepareToPlay(music)
                mediaPlayer?.start()
                binding.ivPlay.setImageResource(R.drawable.icon_btnpause)
                syncWorking()


                }

            R.id.ivNext -> {
                musicStop()
                playNext()
            }


            //한곡 반복재생 / 전체곡 반복재생
            //기본설정: 전체곡 반복
            R.id.ivRepeat -> {
                if(mediaPlayer!!.isLooping){
                    mediaPlayer!!.isLooping = false
                    binding.ivRepeat.setImageResource(R.drawable.icon_repeat_only_this)
                }else {
                    //반복 중이 아닐 때 클릭한 경우
                    mediaPlayer!!.isLooping = true
                    //한곡 무한 반복재생
                    binding.ivRepeat.setImageResource(R.drawable.icon_repeat_all)
                }

            }

            R.id.ivFavorite -> {
                //favorite: 0이면 100을 준다
                if(music!!.favorite == 0){
                    binding.ivFavorite.setImageResource(R.drawable.icon_filled_heart)
//                    music!!.favorite = dbHelper.pressFavorite(music!!)
                    dbHelper.pressFavorite(music!!)

                }else{
                    //favorite: 0이면 100을 준다
                    binding.ivFavorite.setImageResource(R.drawable.icon_empty_heart)
//                    music!!.favorite = dbHelper.pressCancel(music!!)
                    dbHelper.pressCancel(music!!)

                }

            }

            R.id.ivGoBack -> {
                musicStop()
                //메인화면으로 이동하면서 각 음악의 favorite에 대한 인텐트 전달
                val intent = Intent(this,MainActivity::class.java)
//                intent.putExtra("favorite",music?.favorite)
                startActivity(intent)

//                overridePendingTransition(R.anim.slide_down,0)

            }

            R.id.ivFavoritePlaylist -> {
//                mediaPlayer?.pause()
                val intent = Intent(this,FavoritePlaylistActivity::class.java)
                startActivity(intent)
            }


            R.id.ivPlay -> {
            //음악이 재생중일 때
            if(mediaPlayer?.isPlaying == true){
                //일시정지하면
                mediaPlayer?.pause()
                binding.seekBar.progress = mediaPlayer?.currentPosition!!
                //play 이미지로 바뀜
                binding.ivPlay.setImageResource(R.drawable.icon_btnplay)
            }else{
                //음악이 꺼져있는 상태에서 다시 재생시키면
                mediaPlayer?.start()
                //pause 이미지로 바뀜
                binding.ivPlay.setImageResource(R.drawable.icon_btnpause)
            }

                //음악 재생 + seekbar 이동 + 음악의 진행따라 재생시간이 계속 바뀌는걸 보여줌 -> 3개가 동시에 진행
                val backgroundscope = CoroutineScope(Dispatchers.Default + Job())
                playerJob = backgroundscope.launch {

                    //음악의 진행상황을 가져와서 seekbar,시작진행값을 바꿔줘야함
                    while(mediaPlayer?.isPlaying == true){
                        //음악이 현재 재생중인 곳에 맞춰 seekbar 위치값을 설정해줌

                        //중요!! 사용자가 만든 thread에서 동작하는 동안 화면에 view값을 변경하게 되면 문제가 발생함 (화면은 AppCompatActivity이 컨트롤하는 영역이기 때문)
                        //해결방법: thread안의 view값을 변경하고 싶으면 runOnUiThread(~~)를 쓰자

                        runOnUiThread {
                            var currentPosition = mediaPlayer?.currentPosition!!
                            binding.seekBar.progress = currentPosition
                            binding.tvDurationStart.text = SimpleDateFormat("mm:ss").format(currentPosition)
                        }
                        try {
                            delay(500)
                        }catch (e: Exception){
                            Log.d("kim","delay(500) = ${e.toString()}")
                        }

                    }//end of while (mediaPlayer?.isPlaying == true)

                    Log.d("kim","music currentPosition: ${mediaPlayer!!.currentPosition}")
                    Log.d("kim","seekbar max: ${binding.seekBar.max}")

                    //음악이 끝났을 때
                    // 플레이어의 위치값 < seekbar의 max값이기 때문에 차이나는 만큼을 빼줘야함
                    if (mediaPlayer!!.currentPosition >= (binding.seekBar.max - 1000)) {
                        runOnUiThread {
                            // seekbar 초기화
                            binding.seekBar.progress = 0
                            // 음악이 끝나면 경과 시간 초기화
                            binding.tvDurationStart.text = "00:00"
                            // 음악이 끝났으니 일시정지 버튼을 시작 버튼으로 변경
                            binding.ivPlay.setImageResource(R.drawable.icon_btnplay)
                        }
                    }



                }//end of backgroundscope.launch
        }


        }
    }


    //음악이 멈출 때 실행되는 함수
    fun musicStop(){

    //현재 재생되고 있는 음악 멈추고
    mediaPlayer?.stop()
    //코루틴 해제 (seekbar 진행 + 현재 재생시간(위치))
    playerJob?.cancel()
    //seekbar의 진행값이 처음 위치로(0)
    binding.seekBar.progress = 0

}

    fun prepareToPlay(music:Music?){

        //음악이 재생될 때 화면 설정
       if(music != null){
            //====화면과 데이터를 바인딩=====
            binding.tvTitle.text = music.title
            binding.tvArtist.text = music.artist
            binding.tvDurationStart.text = "00:00"
            binding.tvDurationEnd.text = SimpleDateFormat("mm:ss").format(music.duration)

            //====앨범이미지 넣기====
            val bitmap: Bitmap? = music.getAlbumImage(this, ALBUM_IMAGE_SIZE)
            if(bitmap != null) {
                binding.ivAlbumArt.setImageBitmap(bitmap)
            }
            else{
                binding.ivAlbumArt.setImageResource(R.drawable.icon_music_folder)
            }

            //fav값이 100일 때와 0일 때
            if(music.favorite == 100){
                binding.ivFavorite.setImageResource(R.drawable.icon_filled_heart)
            }else{
                binding.ivFavorite.setImageResource(R.drawable.icon_empty_heart)
            }

            //재생할 음원 생성
           //음원 실행 객체 생성
            mediaPlayer = MediaPlayer.create(this, music.getMusicUri())
            //play 이미지가 나오게함
            binding.ivPlay.setImageResource(R.drawable.icon_btnplay)
            //type: long -> toInt()로 형변환
            binding.seekBar.max = music.duration!!.toInt()


           //seekbar에 이벤트 설정해서 음악과 같이 동기화되게 처리한다
            binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                //seekbar을 터치하고 이동시킬 때 발생하는 이벤트
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    //progress: seekBar의 현재 진행값 (Int)
                    //fromUser: Boolean -> 유저에 의한 이동 (true) / 프로그램에 의한 이동 (false)
                    if(fromUser){
                        //seekbar에서 위치값을 가져와 셋팅
                        //재생되는 음악구간
                        mediaPlayer?.seekTo(progress)
                    }
                }

                //seekBar에 터치하는 순간 발생하는 이벤트
                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                //터치를 떼는 순간 이벤트 발생
                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })
        }
    }

    //코루틴 사용 + 노래가 끝나면 다음곡 재생
    fun syncWorking(){

        //음악 재생 + seekbar 이동 + 음악의 진행따라 재생시간이 계속 바뀌는걸 보여줌 -> 3개가 동시에 진행
        val backgroundscope = CoroutineScope(Dispatchers.Default + Job())
        playerJob = backgroundscope.launch {

            //음악의 진행상황을 가져와서 seekbar,시작진행값을 바꿔줘야함
            while(mediaPlayer?.isPlaying == true){
                //음악이 현재 재생중인 곳에 맞춰 seekbar 위치값을 설정해줌

                //중요!! 사용자가 만든 thread에서 동작하는 동안 화면에 view값을 변경하게 되면 문제가 발생함 (화면은 AppCompatActivity이 컨트롤하는 영역이기 때문)
                //해결방법: thread안의 view값을 변경하고 싶으면 runOnUiThread(~~)를 쓰자

                runOnUiThread {
                    var currentPosition = mediaPlayer?.currentPosition!!
                    binding.seekBar.progress = currentPosition
                    binding.tvDurationStart.text = SimpleDateFormat("mm:ss").format(currentPosition)
                }
                try {
                    delay(500)
                }catch (e: Exception){
                    Log.d("kim","delay(500) = ${e.toString()}")
                }

            }//end of while (mediaPlayer?.isPlaying == true)

            Log.d("kim","music currentPosition: ${mediaPlayer!!.currentPosition}")
            Log.d("kim","seekbar max: ${binding.seekBar.max}")

            //음악이 끝났을 때
            // 플레이어의 위치값 < seekbar의 max값이기 때문에 차이나는 만큼을 빼줘야함
            if (mediaPlayer!!.currentPosition >= (binding.seekBar.max - 1000)) {
                runOnUiThread {
                    // seekbar 초기화
                    binding.seekBar.progress = 0
                    // 음악이 끝나면 경과 시간 초기화
                    binding.tvDurationStart.text = "00:00"
                    // 바로 다음곡 재생
                    playNext()
                }
            }



        }//end of backgroundscope.launch

    }

    //다음곡 재생
    fun playNext(){
        //db에 있는 음악 전부 다 가져옴XXX -> 좋아요 누른 곡만 재생하고 싶을 때도 이 기능을 쓰기 때문에 musicList를 항상 selectMusicAll()로 받으면 안됨

        //music은 클릭했을 때 나오는 음악에 대한 정보만을 제공함
        //나는 리스트에서 클릭해서 이동하는게 아니라 플레이화면에서 이동하는것이기 때문에 처음 클릭한 음악을 중심으로 이동해야함 +1 +2...

        //현재 재생중인 음악이 마지막 곡(위치값: 음악목록크기-1)이 아닐 때
        if(position != musicList!!.size - 1) {
            position++
            music = musicList!!.get(position)
        } else{
            //현재 재생중인 음악이 마지막 곡(위치값: 음악목록크기-1)일 때는 다음 곡이 첫번째 곡(위치값: 0)이 된다
            position = 0
            music = musicList!!.get(position)
        }

        prepareToPlay(music)
        mediaPlayer?.start()
        binding.ivPlay.setImageResource(R.drawable.icon_btnpause)
        syncWorking()

    }


}

