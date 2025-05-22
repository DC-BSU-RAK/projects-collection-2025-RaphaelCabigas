package com.example.thebreak

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class BreakAdapter(
    private val data: List<Break>
) : RecyclerView.Adapter<BreakAdapter.BreakItemHolder>() {

    // Initialize the variables like onCreate
    inner class BreakItemHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.breakName)
        val description: TextView = view.findViewById(R.id.breakDescription)
        val image: ImageView = view.findViewById(R.id.breakImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreakItemHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.break_component, parent, false)
        return BreakItemHolder(inflatedView)
    }

    // Get the corresponding values from break_component
    override fun onBindViewHolder(holder: BreakItemHolder, position: Int) {
        val breakValues: Break = data[position]

        holder.name.text = breakValues.name
        holder.description.text = breakValues.description
        holder.image.setImageResource(breakValues.image)
    }

    // Checks how many are the breaks stored
    override fun getItemCount(): Int {
        return data.size
    }
}