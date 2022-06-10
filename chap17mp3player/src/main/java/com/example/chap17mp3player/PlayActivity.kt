package com.example.chap17mp3player

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

    //2. 음악정보 객체 변수
    private var music: Music? = null

    //3. 앨범이미지 사이즈
    private var ALBUM_IMAGE_SIZE = 150

    //4. coroutine scope launch
    private var playerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        music = intent.getSerializableExtra("music") as Music

        //음악이 재생될 때의 화면 설정
        if(music != null){

            binding.tvTitle.text = music?.title
            binding.tvArtist.text = music?.artist
            binding.tvDurationStart.text = "00:00"
            binding.tvDurationEnd.text = SimpleDateFormat("mm:ss").format(music?.duration)

            val bitmap: Bitmap? = music?.getAlbumImage(this,ALBUM_IMAGE_SIZE)

            if(bitmap != null){
                binding.ivAlbumArt.setImageBitmap(bitmap)
            }else{
                binding.ivAlbumArt.setImageResource(R.drawable.music_24)
            }

            //음원 실행 객체 생성 및 음악 재생
            mediaPlayer = MediaPlayer.create(this,music?.getMusicUri())
            binding.seekBar.max = music?.duration!!.toInt()

            //seekbar에 이벤트 설정해서 음악과 같이 동기화되게 처리한다
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                //seekbar을 터치하고 이동할 때 발생되는 이벤트
                //user에 의한 터치 유무인지 점검
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if(fromUser){
                        //재생되는 음악구간: seekbar에서 위치값을 가져와 셋팅
                        mediaPlayer?.seekTo(progress)
                    }
                }

                //seekbar를 터치하는 순간 이벤트 발생
                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                //터치를 떼는 순간 이벤트 발생
                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })


        }//end of when music is playing



    }

        fun onClickView(view: View?){
            when(view?.id){

                //누르면 음악을 멈추고 코루틴을 취소함
                R.id.ivMain->{
                    mediaPlayer?.stop()
                    playerJob?.cancel()
                    finish()
                }

                R.id.ivPlay->{
                    //음악이 재생중일 때
                    if(mediaPlayer?.isPlaying == true){
                        //일시정지하면
                        mediaPlayer?.pause()
                        binding.seekBar.progress = mediaPlayer?.currentPosition!!
                        //play 이미지로 바뀜
                        binding.ivPlay.setImageResource(R.drawable.play_24)
                    }else{
                        //음악이 꺼져있는 상태에서 다시 재생시키면
                        mediaPlayer?.start()
                        //pause 이미지로 바뀜
                        binding.ivPlay.setImageResource(R.drawable.pause_24)}

                        //음악 재생 + seekbar 진행 + 음악의 진행따라 재생시간이 계속 바뀌는걸 보여줌: 3개가 동시에 진행되어야함
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

                            //음악이 끝났을 때 플레이어의 위치값 < seekbar의 max값이기 때문에 차이나는 만큼을 빼줘야함
                            if (mediaPlayer!!.currentPosition >= (binding.seekBar.max - 1000)) {
                                runOnUiThread {
                                    // seekbar 초기화
                                    binding.seekBar.progress = 0
                                    // 음악이 끝나면 경과 시간 초기화
                                    binding.tvDurationStart.text = "00:00"
                                    // 음악이 끝났으니 일시정지 버튼을 시작 버튼으로 변경
                                    binding.ivPlay.setImageResource(R.drawable.play_24)
                                }
                            }



                        }//end of backgroundscope.launch

                }

//                R.id.ivStop->{
//                    mediaPlayer?.stop()
//                    playerJob?.cancel()
//                    mediaPlayer = MediaPlayer.create(this,music?.getMusicUri())
//                    //다른 음악을 재생시킬 수 있도록 다시 원위치 시킴
//                    binding.seekBar.progress = 0
//                    binding.tvDurationStart.text = "00:00"
//                    binding.ivPlay.setImageResource(R.drawable.play_24)
//                }
            }
        }


}