package jiho.mydressroom.org.mydressroomapplication.Adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import jiho.mydressroom.org.mydressroomapplication.Activity.MainActivity;
import jiho.mydressroom.org.mydressroomapplication.R;
import jiho.mydressroom.org.mydressroomapplication.Items.ChatItems;

public class ChatAdapter extends FirebaseRecyclerAdapter<ChatItems,ChatAdapter.BoardViewHolder>{
    public ChatAdapter(FirebaseRecyclerOptions options) {
        super( options );
    }
    public class BoardViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_board;
        TextView tv_boardName,tv_boardTitle,tv_boardText;

        public BoardViewHolder(View v) {
            super(v);
            tv_boardName = itemView.findViewById( R.id.tv_boardName);
            tv_boardTitle = itemView.findViewById(R.id.tv_boardTitle);
            tv_boardText = itemView.findViewById(R.id.tv_boardText);
            iv_board = itemView.findViewById( R.id.iv_board );
        }
    }
    @Override
    public BoardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chatitems, parent, false);
        return new BoardViewHolder(view);
    }
    @Override
    protected void onBindViewHolder(BoardViewHolder holder, int position, ChatItems model) {
        holder.tv_boardName.setText(model.getName());
        holder.tv_boardTitle.setText(model.getTitle());
        holder.tv_boardText.setText(model.getText());
        if (model.getUri() == null) {
            holder.iv_board.setImageDrawable( ContextCompat.getDrawable(new MainActivity().getApplicationContext(),
                    R.drawable.common_google_signin_btn_icon_dark_normal));
        } else {
            Glide.with(holder.iv_board)
                    .load(model.getUri())
                    .into(holder.iv_board);
        }
    }
}
