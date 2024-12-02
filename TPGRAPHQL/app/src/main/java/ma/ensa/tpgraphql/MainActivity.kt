package ma.ensa.tpgraphql

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import ma.ensa.tpgraphql.adapter.CompteAdapter
import ma.ensa.tpgraphql.type.TypeCompte
import ma.ensa.tpgraphql.viewmodel.CompteViewModel

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var viewModel: CompteViewModel
    private lateinit var adapter: CompteAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: Button
    private lateinit var typeFilterSpinner: Spinner
    private lateinit var totalComptesTextView: TextView
    private lateinit var totalSoldeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "MainActivity created")

        setupViews()
        setupViewModel()
        setupObservers()
        viewModel.fetchComptes()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        typeFilterSpinner = findViewById(R.id.typeFilterSpinner)
        totalComptesTextView = findViewById(R.id.totalComptesTextView)
        totalSoldeTextView = findViewById(R.id.totalSoldeTextView)

        adapter = CompteAdapter { compteId ->
            showDeleteConfirmationDialog(compteId)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = this@MainActivity.adapter
        }

        fabAdd.setOnClickListener {
            showAddCompteDialog()
        }

        setupTypeSpinner()
    }

    private fun setupTypeSpinner() {
        val types = mutableListOf("ALL") + TypeCompte.knownValues().map { it.rawValue }
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeFilterSpinner.adapter = typeAdapter

        typeFilterSpinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedType = parent?.getItemAtPosition(position).toString()
                    if (selectedType == "ALL") {
                        viewModel.fetchComptes()
                    } else {
                        val type = TypeCompte.safeValueOf(selectedType)
                        viewModel.filterComptesByType(type)
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    // Do nothing
                }
            }
    }

    private fun setupViewModel() {
        viewModel = CompteViewModel()
    }

    private fun setupObservers() {
        viewModel.comptes.observe(this) { comptes ->
            Log.d(TAG, "Received ${comptes.size} comptes")
            adapter.updateData(comptes)
        }

        viewModel.error.observe(this) { error ->
            Log.e(TAG, "Error received: $error")
            Toast.makeText(this, "Erreur: $error", Toast.LENGTH_LONG).show()
        }

        viewModel.totalComptes.observe(this) { total ->
            totalComptesTextView.text = "Total Comptes: $total"
        }

        viewModel.totalSolde.observe(this) { totalSolde ->
            totalSoldeTextView.text = "Total Solde: $totalSolde DH"
        }
    }

    private fun showDeleteConfirmationDialog(compteId: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Supprimer le compte")
            .setMessage("Êtes-vous sûr de vouloir supprimer ce compte?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deleteCompte(compteId)
                // Refresh the current filter after deletion
                val currentFilter = typeFilterSpinner.selectedItem.toString()
                if (currentFilter == "ALL") {
                    viewModel.fetchComptes()
                } else {
                    viewModel.filterComptesByType(TypeCompte.safeValueOf(currentFilter))
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showAddCompteDialog() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_compte, null)

        val soldeInput = dialogView.findViewById<TextInputEditText>(R.id.soldeInput)
        val typeSpinner = dialogView.findViewById<Spinner>(R.id.typeSpinner)

        val validTypes = TypeCompte.knownValues().map { it.rawValue }
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, validTypes)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        MaterialAlertDialogBuilder(this)
            .setTitle("Ajouter un compte")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { dialog, _ ->
                val soldeText = soldeInput.text?.toString()
                val selectedType = typeSpinner.selectedItem?.toString()

                when {
                    soldeText.isNullOrEmpty() || selectedType.isNullOrEmpty() -> {
                        Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> try {
                        val solde = soldeText.toDouble()
                        val type = TypeCompte.safeValueOf(selectedType)
                        val currentFilter = typeFilterSpinner.selectedItem.toString()

                        viewModel.addCompte(solde, type)

                        // Wait briefly to ensure the add operation completes
                        recyclerView.postDelayed({
                            if (currentFilter == "ALL") {
                                viewModel.fetchComptes()
                            } else {
                                viewModel.filterComptesByType(TypeCompte.safeValueOf(currentFilter))
                            }
                        }, 100) // Small delay to ensure the add operation completes

                    } catch (e: NumberFormatException) {
                        Toast.makeText(this, "Solde invalide", Toast.LENGTH_SHORT).show()
                    } catch (e: IllegalArgumentException) {
                        Toast.makeText(this, "Type invalide", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}