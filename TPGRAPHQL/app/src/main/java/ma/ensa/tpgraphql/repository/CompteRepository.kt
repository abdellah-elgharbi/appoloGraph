package ma.ensa.tpgraphql.repository

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ma.ensa.tpgraphql.*
import ma.ensa.tpgraphql.type.TypeCompte
import ma.ensa.tpgraphql.type.CompteInput

class CompteRepository {
    private val TAG = "CompteRepository"

    private val apolloClient = ApolloClient.Builder()
        .serverUrl("http://100.79.105.22:8088/graphql")
        .build()

    suspend fun getAllComptes(): Result<List<GetAllComptesQuery.AllCompte>> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching all comptes...")
                val response = apolloClient.query(GetAllComptesQuery()).execute()

                if (response.hasErrors()) {
                    val error = response.errors?.first()?.message ?: "Unknown error"
                    Log.e(TAG, "Error fetching comptes: $error")
                    Result.failure(Exception(error))
                } else {
                    val comptes = response.data?.allComptes?.filterNotNull() ?: emptyList()
                    Log.d(TAG, "Fetched ${comptes.size} comptes")
                    Result.success(comptes)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while fetching comptes", e)
                Result.failure(e)
            }
        }

    suspend fun findByType(type: TypeCompte): Result<List<FindByTypeQuery.FindByType>> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching comptes by type: $type")
                val response = apolloClient.query(FindByTypeQuery(type)).execute()

                if (response.hasErrors()) {
                    val error = response.errors?.first()?.message ?: "Unknown error"
                    Log.e(TAG, "Error fetching comptes by type: $error")
                    Result.failure(Exception(error))
                } else {
                    val comptes = response.data?.findByType?.filterNotNull() ?: emptyList()
                    Log.d(TAG, "Fetched ${comptes.size} comptes of type $type")
                    Result.success(comptes)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while fetching comptes by type", e)
                Result.failure(e)
            }
        }

    suspend fun saveCompte(solde: Double, type: TypeCompte): Result<SaveCompteMutation.SaveCompte> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Saving compte with solde: $solde and type: $type")
                val input = CompteInput(
                    solde = solde,
                    type = Optional.Present(type)
                )

                val response = apolloClient.mutation(SaveCompteMutation(input)).execute()

                if (response.hasErrors()) {
                    val error = response.errors?.first()?.message ?: "Unknown error"
                    Log.e(TAG, "Error saving compte: $error")
                    Result.failure(Exception(error))
                } else {
                    response.data?.saveCompte?.let { savedCompte ->
                        Log.d(TAG, "Compte saved successfully with id: ${savedCompte.id}")
                        Result.success(savedCompte)
                    } ?: Result.failure(Exception("Save response was null"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while saving compte", e)
                Result.failure(e)
            }
        }

    suspend fun deleteCompte(compteId: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Deleting compte with id: $compteId")
                val response = apolloClient.mutation(DeleteCompteMutation(compteId)).execute()

                if (response.hasErrors()) {
                    val error = response.errors?.first()?.message ?: "Unknown error"
                    Log.e(TAG, "Error deleting compte: $error")
                    Result.failure(Exception(error))
                } else {
                    val result = response.data?.deleteCompte
                    if (result != null) {
                        Log.d(TAG, "Compte deleted successfully with id: $compteId")
                        Result.success(result)
                    } else {
                        Result.failure(Exception("Delete response was null"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while deleting compte", e)
                Result.failure(e)
            }
        }
}