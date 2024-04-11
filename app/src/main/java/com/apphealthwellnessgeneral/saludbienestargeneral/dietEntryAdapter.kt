package com.apphealthwellnessgeneral.saludbienestargeneral

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class dietEntryAdapter(private val dietEntries: List<dietEntryModel>) :
    RecyclerView.Adapter<dietEntryAdapter.DietEntryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DietEntryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diet_entry, parent, false)
        return DietEntryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DietEntryViewHolder, position: Int) {
        val currentItem = dietEntries[position]

        holder.textFoodName.text = currentItem.foodName
        holder.textMealType.text = currentItem.mealType
        holder.textPortion.text = currentItem.portion
        holder.textCalories.text = "Calorías: ${currentItem.calories}"
        holder.textDate.text = "Fecha: ${currentItem.date}"
        holder.textTime.text = "Hora: ${currentItem.time}"
    }

    override fun getItemCount() = dietEntries.size

    inner class DietEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textFoodName: TextView = itemView.findViewById(R.id.textFoodName)
        val textMealType: TextView = itemView.findViewById(R.id.textMealType)
        val textCalories: TextView = itemView.findViewById(R.id.textCalories)
        val textPortion : TextView = itemView.findViewById(R.id.textPortion)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val textTime: TextView = itemView.findViewById(R.id.textTime)
    }
}
