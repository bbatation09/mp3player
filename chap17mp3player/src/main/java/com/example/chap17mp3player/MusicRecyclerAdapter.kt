package com.example.chap17mp3player

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chap17mp3player.databinding.ItemRecyclerBinding

class MusicRecyclerAdapter(val context: Context, val musiclist: MutableList<Music>?): RecyclerView.Adapter<MusicRecyclerAdapter.CustomViewHolder>(){
    //이미지 사이즈 정의함
    val ALBUM_IMAGE_SIZE = 100

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder{
        var binding = ItemRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        //다운캐스팅
        val binding = (holder as CustomViewHolder).binding

        //데이터 리스트에서 가져온 <index: position>인 데이터 중 화면에 출력해야하는 정보를 전달해줌
        val music = musiclist?.get(position)
        binding.tvArtist.text = music?.artist
        binding.tvTitle.text = music?.title
        binding.tvduration.text = java.text.SimpleDateFormat("mm:ss").format(music?.duration)

        val bitmap: Bitmap? = music?.getAlbumImage(context,ALBUM_IMAGE_SIZE)

        //앨범이미지가 없을 때 디폴트 이미지를 띄움
        if(bitmap != null){
            binding.ivAlbumArt.setImageBitmap(bitmap)
        }else{
            binding.ivAlbumArt.setImageResource(R.drawable.music_24)
        }

        //모든 뷰를 포함하고 있는 레이아웃: binding.root

        //음악을 재생시키기 위해 클릭하면 내가 클릭한 음악에 대한 객체를 갖고 플레이액티비티로 넘어간다
        binding.root.setOnClickListener {
            val intent = Intent(binding.root.context,PlayActivity::class.java)
            intent.putExtra("music", music)
            binding.root.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return musiclist?.size?:0
    }

    //뷰홀더 내부선언 (바인딩)
    class CustomViewHolder(val binding: ItemRecyclerBinding): RecyclerView.ViewHolder(binding.root)
}