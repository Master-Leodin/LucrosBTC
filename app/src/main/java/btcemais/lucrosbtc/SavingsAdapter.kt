package btcemais.lucrosbtc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class SavingsAdapter(private val onDeleteClick: (Savings) -> Unit) :
    ListAdapter<Savings, SavingsAdapter.SavingsViewHolder>(SavingsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saving, parent, false)
        return SavingsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavingsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SavingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        private val amountText: TextView = itemView.findViewById(R.id.amountText)
        private val btcText: TextView = itemView.findViewById(R.id.btcText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(savings: Savings) {
            descriptionText.text = savings.description

            val symbol = when(savings.currency) {
                "usd" -> "US$"
                "eur" -> "€"
                else -> "R$"
            }

            val amountTextValue = if (savings.amount < 0) {
                "-$symbol ${"%.2f".format(abs(savings.amount))}"
            } else {
                "+$symbol ${"%.2f".format(savings.amount)}"
            }

            amountText.text = amountTextValue

            val btcAmount = savings.sats.toDouble() / 100_000_000
            val formattedBtc = String.format(Locale.US, "%.8f", btcAmount).replace(",", ".")
            btcText.text = if (savings.sats < 0) "-$formattedBtc BTC" else "$formattedBtc BTC"

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateText.text = dateFormat.format(savings.date)

            val colorRes = if (savings.amount < 0) R.color.red else R.color.green
            val color = ContextCompat.getColor(itemView.context, colorRes)

            amountText.setTextColor(color)
            btcText.setTextColor(color)

            deleteButton.setOnClickListener {
                onDeleteClick(savings)
            }
        }
    }

    class SavingsDiffCallback : DiffUtil.ItemCallback<Savings>() {
        override fun areItemsTheSame(oldItem: Savings, newItem: Savings): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Savings, newItem: Savings): Boolean {
            return oldItem == newItem
        }
    }
}