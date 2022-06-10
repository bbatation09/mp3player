package com.example.chap17mp3player

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chap17mp3player.databinding.ItemSongBinding

class FavoritePlaylistAdapter(val context: Context, val musiclist: MutableList<Music>?): RecyclerView.Adapter<FavoritePlaylistAdapter.CustomViewHolder>(){
    //이미지 사이즈 정의함
    val ALBUM_IMAGE_SIZE = 100

    //DB 객체화
    val dbHelper : DBHelper by lazy {
        DBHelper(context,"musicTBL",1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder{
        var binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CustomViewHolder(binding)
    }

    //데이터와 뷰 연결
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        //다운캐스팅
        val binding = holder.binding

        //데이터 리스트에서 가져온 위치가 position인 데이터 중 화면에 출력해야하는 정보를 전달해줌
        val music = musiclist?.get(position)
        binding.tvArtist.text = music?.artist
        binding.tvTitle.text = music?.title

        if(music!!.favorite == 0){
            binding.ivFavorite.setImageResource(R.drawable.icon_empty_heart)
//            dbHelper.pressCancel(music)

        }else{
            binding.ivFavorite.setImageResource(R.drawable.icon_filled_heart)
//            dbHelper.pressFavorite(music)
        }



        val bitmap: Bitmap? = music?.getAlbumImage(context,ALBUM_IMAGE_SIZE)

        //앨범이미지가 없을 때 디폴트 이미지를 띄움
        if(bitmap != null){
            binding.ivAlbumArt.setImageBitmap(bitmap)
        }else{
            binding.ivAlbumArt.setImageResource(R.drawable.icon_music_folder)
        }


        binding.ivFavorite.setOnClickListener {
            dbHelper.pressCancel(music)
            binding.ivFavorite.setImageResource(R.drawable.icon_empty_heart)
        }

        //모든 뷰를 포함하고 있는 레이아웃: binding.root

        //음악을 재생시키기 위해 클릭하면 내가 클릭한 음악에 대한 객체를 갖고 playActivity(재생화면)으로 넘어감
        binding.ivPlay.setOnClickListener {
            val intent = Intent(binding.root.context,PlayActivity::class.java)
            intent.putExtra("music", music)
            intent.putExtra("position",position)
            intent.putExtra("from","favorite")
            binding.root.context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return musiclist?.size?:0
    }

    //뷰홀더 내부선언 (바인딩)
    class CustomViewHolder(val binding: ItemSongBinding): RecyclerView.ViewHolder(binding.root)

}