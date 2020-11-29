package com.example.project1

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class ListAdapterChat(context : Context, dataList : List<DatabaseClasses.Chat>) : RecyclerView.Adapter<ListAdapterChat.ChatViewHolder>() {
    private var inflater : LayoutInflater = LayoutInflater.from(context)
    private var dataList = dataList

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderTextView: TextView = itemView.findViewById<View>(R.id.listitem_chat_sender) as TextView
        val messageTextView: TextView = itemView.findViewById<View>(R.id.listitem_chat_message) as TextView
        val containerLayout: ConstraintLayout = itemView.findViewById(R.id.listitem_chat_item) as ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        // Inflate an item view.
        val mItemView: View = inflater.inflate(R.layout.listitem_chats, parent, false)
        return ChatViewHolder(mItemView)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        // Retrieve the data for that position.
        val currentChat: DatabaseClasses.Chat = dataList[position]
        // Add the data to the view holder.
        holder.senderTextView.text = currentChat.sender;
        holder.messageTextView.text = currentChat.message;
        if(currentChat.position == 0){  //disagree
            holder.containerLayout.setBackgroundColor( Color.parseColor("#FF9494") )
        } else if (currentChat.position == 2) { //admin
            holder.containerLayout.setBackgroundColor( Color.parseColor("#FFEB3B") )
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}