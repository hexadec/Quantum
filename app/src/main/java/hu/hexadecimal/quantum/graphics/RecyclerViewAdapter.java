package hu.hexadecimal.quantum.graphics;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import hu.hexadecimal.quantum.R;
import hu.hexadecimal.quantum.math.VisualOperator;

/**
 * Used in the gate editor to display the gates
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private final List<VisualOperator> operators;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;

    // data is passed into the constructor
    public RecyclerViewAdapter(Context context, List<VisualOperator> data) {
        this.mInflater = LayoutInflater.from(context);
        this.operators = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.gate_display, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VisualOperator operator = operators.get(position);
        holder.gateName.setText(operator.getName());
        holder.color.setBackgroundColor(operator.getColor());
        StringBuilder symbol = new StringBuilder();
        for (int i = 0; i < operator.getSymbols().length; i++) {
            if (i != 0) symbol.append('\u2003');
            symbol.append("<b>q");
            symbol.append(i + 1);
            symbol.append("</b>:\u2002");
            symbol.append(operator.getSymbols()[i]);
        }
        holder.itemView.setLongClickable(true);
        holder.gateSymbols.setText(Html.fromHtml(symbol.toString()));
        holder.gateMatrix.loadData(operator.toStringHtmlTable(), "text/html", "UTF-8");
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return operators.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final TextView gateName;
        final TextView gateSymbols;
        final WebView gateMatrix;
        final View color;

        ViewHolder(View itemView) {
            super(itemView);
            gateName = itemView.findViewById(R.id.gate_name_disp);
            gateSymbols = itemView.findViewById(R.id.gate_symbols_disp);
            gateMatrix = itemView.findViewById(R.id.gate_matrix_disp);
            color = itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (mLongClickListener != null) return mLongClickListener.onItemLongClick(view, getAdapterPosition());
            return false;
        }
    }

    // convenience method for getting data at click position
    VisualOperator getItem(int id) {
        return operators.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface ItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }
}
