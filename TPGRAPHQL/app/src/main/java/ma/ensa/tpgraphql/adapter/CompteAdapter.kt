package ma.ensa.tpgraphql.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.tpgraphql.R
import ma.ensa.tpgraphql.GetAllComptesQuery

class CompteAdapter(
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<CompteAdapter.CompteViewHolder>() {
    private var comptes = mutableListOf<GetAllComptesQuery.AllCompte>()

    inner class CompteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        val soldeTextView: TextView = itemView.findViewById(R.id.soldeTextView)
        val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compte, parent, false)

        // Appliquer un fond à la vue
        view.setBackgroundResource(R.drawable.background_item_compte) // Assurez-vous de définir un drawable dans res/drawable/background_item_compte.xml

        return CompteViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompteViewHolder, position: Int) {
        val compte = comptes[position]
        holder.apply {
            idTextView.text = "ID: ${compte.id}"
            soldeTextView.text = "${compte.solde} $"
            typeTextView.text = "${compte.type}"

            deleteButton.setOnClickListener {
                onDeleteClick(compte.id)
            }
        }
    }

    override fun getItemCount() = comptes.size

    fun updateData(newComptes: List<GetAllComptesQuery.AllCompte>) {
        comptes.clear()
        comptes.addAll(newComptes)
        notifyDataSetChanged()
    }
}