package com.aam.mcu.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aam.mcu.R;
import com.aam.mcu.data.Quiz;

import java.util.ArrayList;

public class QuizRecycler extends RecyclerView.Adapter<QuizRecycler.ViewHolder> {

    ArrayList<Quiz> quizzes;
    Context context;

    public QuizRecycler(Context context, ArrayList<Quiz> quizzes) {
        this.context = context;
        this.quizzes = quizzes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_quiz_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.tv_title.setText(quizzes.get(position).getTitle());
        holder.tv_time.setText(quizzes.get(position).getTime());

        holder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quizzes.get(position).getLink() != null){
                    //load url for quiz
                    Intent intent = new Intent(context, com.aam.mcu.Quiz.class);
                    intent.putExtra("url", quizzes.get(position).getLink());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title,  tv_time;
        RelativeLayout parentView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_title = itemView.findViewById(R.id.sqv_tv_title);
            tv_time = itemView.findViewById(R.id.sqv_tv_time);
            parentView = itemView.findViewById(R.id.sqv_parent);
        }
    }
}
