package ma.ensa.tpgraphql.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ma.ensa.tpgraphql.GetAllComptesQuery
import ma.ensa.tpgraphql.FindByTypeQuery
import ma.ensa.tpgraphql.repository.CompteRepository
import ma.ensa.tpgraphql.type.TypeCompte

class CompteViewModel : ViewModel() {
    private val TAG = "CompteViewModel"
    private val repository = CompteRepository()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val _totalComptes = MutableLiveData<Int>()
    val totalComptes: LiveData<Int> = _totalComptes

    private val _totalSolde = MutableLiveData<Double>()
    val totalSolde: LiveData<Double> = _totalSolde

    private val _comptes = MutableLiveData<List<GetAllComptesQuery.AllCompte>>()
    val comptes: LiveData<List<GetAllComptesQuery.AllCompte>> = _comptes

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchComptes() {
        coroutineScope.launch(Dispatchers.Main) {
            repository.getAllComptes().fold(
                onSuccess = { comptesList ->
                    _comptes.value = comptesList
                    updateStatistics(comptesList)
                    Log.d(TAG, "Successfully fetched ${comptesList.size} comptes")
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e(TAG, "Error fetching comptes", exception)
                }
            )
        }
    }

    fun filterComptesByType(type: TypeCompte) {
        coroutineScope.launch(Dispatchers.Main) {
            repository.findByType(type).fold(
                onSuccess = { comptesList ->
                    // Map FindByType results to match AllCompte type
                    val mappedComptes = comptesList.map { findByTypeCompte ->
                        GetAllComptesQuery.AllCompte(
                            id = findByTypeCompte.id,
                            solde = findByTypeCompte.solde,
                            dateCreation = findByTypeCompte.dateCreation,
                            type = findByTypeCompte.type
                        )
                    }
                    _comptes.value = mappedComptes
                    updateStatistics(mappedComptes)
                    Log.d(TAG, "Successfully filtered ${mappedComptes.size} comptes of type $type")
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e(TAG, "Error filtering comptes by type", exception)
                }
            )
        }
    }


    fun addCompte(solde: Double, type: TypeCompte) {
        coroutineScope.launch(Dispatchers.Main) {
            repository.saveCompte(solde, type).fold(
                onSuccess = { savedCompte ->
                    Log.d(TAG, "Successfully added compte with id: ${savedCompte.id}")
                    // Don't fetch all comptes here - let the UI handle the refresh
                    // fetchComptes() // Remove this line
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e(TAG, "Error adding compte", exception)
                }
            )
        }
    }

    fun deleteCompte(compteId: String) {
        coroutineScope.launch(Dispatchers.Main) {
            repository.deleteCompte(compteId).fold(
                onSuccess = {
                    Log.d(TAG, "Successfully deleted compte with id: $compteId")
                    fetchComptes()
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e(TAG, "Error deleting compte", exception)
                }
            )
        }
    }

    private fun updateStatistics(comptesList: List<GetAllComptesQuery.AllCompte>) {
        _totalComptes.value = comptesList.size
        _totalSolde.value = comptesList.sumOf { it.solde.toDouble() }
    }


}