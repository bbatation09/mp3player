package com.example.chap17mp3player

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chap17mp3player.databinding.ItemSongBinding

class MusicRecyclerAdapter(val context: Context, val musiclist: MutableList<Music>?): RecyclerView.Adapter<MusicRecyclerAdapter.CustomViewHolder>(){

    //앨범 이미지 사이즈 정의함
    val ALBUM_IMAGE_SIZE = 100

    //DB 객체를 실행함 *데이터베이스 파일이 있으면 또 만들지 않는다
    //만들어진 데이터베이스 객체만 전달함
    val dbHelper : DBHelper by lazy {
        DBHelper(context,"musicTBL",1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder{
        var binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CustomViewHolder(binding)
    }

    //데이터와 뷰 연결
    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        //다운캐스팅
        val binding = holder.binding

        //데이터 리스트에서 가져온 위치가 position인 데이터 중 화면에 출력해야하는 정보를 전달해줌
        //데이터: musiclist 와 뷰: item_song 연결
        val music = musiclist?.get(position)
        binding.tvArtist.text = music?.artist
        binding.tvTitle.text = music?.title

        when(music?.favorite){
            0 -> binding.ivFavorite.setImageResource(R.drawable.icon_empty_heart)
            100 -> binding.ivFavorite.setImageResource(R.drawable.icon_filled_heart)
        }

    val bitmap: Bitmap? = music?.getAlbumImage(context,ALBUM_IMAGE_SIZE)

    //앨범이미지가 없을 때 디폴트 이미지를 띄움
    if(bitmap != null){
        binding.ivAlbumArt.setImageBitmap(bitmap)
    }else{
        binding.ivAlbumArt.setImageResource(R.drawable.icon_music_folder)
    }


    //모든 뷰를 포함하고 있는 레이아웃: binding.root

    //음악을 재생시키기 위해 클릭하면 내가 클릭한 음악에 대한 객체를 갖고 playActivity(재생화면)으로 넘어감
    binding.ivPlay.setOnClickListener {
        val intent = Intent(binding.root.context,PlayActivity::class.java)
        intent.putExtra("music", music)
        intent.putExtra("position",position)
        intent.putExtra("from","main")
        binding.root.context.startActivity(intent)
    }

    binding.ivFavorite.setOnClickListener {
        var updateFlag = false
        if(music?.favorite == 0) {
            binding.ivFavorite.setImageResource(R.drawable.icon_filled_heart)
            music?.favorite = 100
        }else{
            binding.ivFavorite.setImageResource(R.drawable.icon_empty_heart)
            music?.favorite= 0
            if (music != null) {
                updateFlag = dbHelper.setFavoriteData(music)
                if(updateFlag == false){
                    Log.d("kim","favorite 업데이트 실패 ${music.toString()}")
                }
                //데이터 무효화 영역 처리를 통해서 화면을 다시 재생시킴(내용을 업데이트 시킴)
                notifyDataSetChanged()
            }

        }




    }






}

override fun getItemCount(): Int {
    return musiclist?.size?:0
}

//뷰홀더 내부선언 (바인딩)
class CustomViewHolder(val binding: ItemSongBinding): RecyclerView.ViewHolder(binding.root)

}



