package com.team4.spotifywrapped;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.team4.spotifywrapped.data.PreviousWrappedSelectItem;
import com.team4.spotifywrapped.interfaces.ClickListener;

import java.lang.ref.WeakReference;
import java.util.List;

public class PreviousWrappedAdapter extends RecyclerView.Adapter<PreviousWrappedAdapter.ViewHolder> {

    private final ClickListener listener;
    private final List<PreviousWrappedSelectItem> itemsList;

    public PreviousWrappedAdapter(List<PreviousWrappedSelectItem> itemsList, ClickListener listener) {
        this.listener = listener;
        this.itemsList = itemsList;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.previous_wrapped_item, parent, false), listener);
    }

    @Override public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // bind layout and data etc..
        viewHolder.getButton().setText("" + Integer.toString(itemsList.get(position).getPos()) + itemsList.get(position).getText());
    }

    @Override public int getItemCount() {
        return itemsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private Button button;
        private WeakReference<ClickListener> listenerRef;

        public ViewHolder(final View itemView, ClickListener listener) {
            super(itemView);

            listenerRef = new WeakReference<>(listener);
            button = (Button) itemView.findViewById(R.id.previous_wrapped_btn);

            itemView.setOnClickListener(this);
            button.setOnClickListener(this);
        }

        // onClick Listener for view
        @Override
        public void onClick(View v) {

            if (v.getId() == button.getId()) {
                Toast.makeText(v.getContext(), "ITEM PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }

            listenerRef.get().onPositionClicked(getAdapterPosition());
        }


        //onLongClickListener for view
        @Override
        public boolean onLongClick(View v) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Hello Dialog")
                    .setMessage("LONG CLICK DIALOG WINDOW FOR ICON " + String.valueOf(getAdapterPosition()))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            builder.create().show();
            listenerRef.get().onLongClicked(getAdapterPosition());
            return true;
        }

        public Button getButton() {
            return button;
        }
    }}
