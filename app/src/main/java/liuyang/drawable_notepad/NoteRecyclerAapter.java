package liuyang.drawable_notepad;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/5/28.
 */
public class NoteRecyclerAapter extends RecyclerView.Adapter<NoteRecyclerAapter.ViewHolder> {
    private Context context;
    private int layoutResourceId;
    private List<Note> data = null;
    private int mPosition=-1;


    public int getPosition() {
        return mPosition;
    }
    public Note getPositionItem(){
        return data.get(mPosition);
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView name;
        TextView content;
        TextView date;
        // ImageView photo;
        public ViewHolder(View itemView) {
            super(itemView);
            name= (TextView) itemView.findViewById(R.id.noteTitle);
            content= (TextView) itemView.findViewById(R.id.noteContent);
            date= (TextView) itemView.findViewById(R.id.noteDate);
            // photo= (ImageView) itemView.findViewById(R.id.note_photo);
            itemView.setOnCreateContextMenuListener(this);//上下文菜单

        }
        //上下文菜单
     @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         menu.setHeaderTitle("请选择");
         menu.add(ContextMenu.NONE, 0, ContextMenu.NONE, "添加");
         menu.add(ContextMenu.NONE, 1, ContextMenu.NONE, "删除");
         menu.add(ContextMenu.NONE, 2, ContextMenu.NONE, "修改");
         menu.add(ContextMenu.NONE, 3, ContextMenu.NONE, "分享");
         menu.add(ContextMenu.NONE, 4, ContextMenu.NONE, "取消");

        }
    }

    public NoteRecyclerAapter(Context context, int layoutResourceId, List<Note> data){
        super();
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        //resolver=context.getContentResolver();
    }
    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_item_row, viewGroup, false);
        final ViewHolder holder=new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int postion = holder.getAdapterPosition();
                mPosition=postion;
                Intent intent = new Intent(viewGroup.getContext(), NoteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("id", String.valueOf(data.get(postion).getId()));
                viewGroup.getContext().startActivity(intent);
            }
        });
        return holder;
    }



    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        Note note = data.get(i);
        String noteTitle = note.getTitle();
        if (noteTitle == null || noteTitle.length() == 0)
            noteTitle = String.format(context.getString(R.string.note_number), note.getId());
        viewHolder.name.setText(noteTitle);

        String title = note.getRawText();
        if (title.length() != 0) {
            viewHolder.content.setText(note.getRawText());
        } else {
            //TODO Finding out if there is picture on note
            viewHolder.content.setText("没有内容");
        }
        viewHolder.date.setText(note.getFormattedDateUpdatted());
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setPosition(viewHolder.getLayoutPosition());
                return false;
            }
        });

    }






    /**
     * 逻辑5：在Adapter中设置一个过滤方法，目的是为了将过滤后的数据传入Adapter中并刷新数据
     * @param locationListModels
     */
    public void setFilter(ArrayList<Note> locationListModels ) {

        data = new ArrayList<>();

        data .addAll(locationListModels);

        notifyDataSetChanged();

    }

    public List<Note> getData() {
        return data;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
