package hu.hexadecimal.quantum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<VisualOperator> operators;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    RecyclerViewAdapter(Context context, List<VisualOperator> data) {
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
        if (operator instanceof LinearOperator) {
            holder.gateSymbols.setText(((LinearOperator)operator).getSymbol());
            holder.gateMatrix.setText(((LinearOperator)operator).toString());
        } else if (operator instanceof MultiQubitOperator) {
            StringBuilder symbol = new StringBuilder();
            for (String s : ((MultiQubitOperator)operator).getSymbols()) {
                symbol.append("  ");
                symbol.append(s);
            }
            holder.gateSymbols.setText(symbol.toString());
            holder.gateMatrix.setText(((MultiQubitOperator)operator).toString());
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return operators.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView gateName;
        TextView gateSymbols;
        TextView gateMatrix;
        View color;

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
    }

    // convenience method for getting data at click position
    VisualOperator getItem(int id) {
        return operators.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}