package com.group5.gue.api

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

/**
 * Interface defining the base structure for all data repositories.
 * Provides a common foundation for interacting with Supabase tables.
 */
interface BaseRepository {
    // The name of the table in Supabase this repository manages.
    val tableName: String
    // The singleton Supabase client instance.
    val client: SupabaseClient get() = SupabaseProvider.supabaseClient
    // Tag used for logging purposes, defaults to the class name.
    val tag: String get() = this::class.simpleName ?: "BaseRepository"
}

/**
 * Fetches a single record from the database based on a column-value pair.
 * 
 * @param column The name of the column to filter by.
 * @param value The value the column must match.
 * @return The decoded object of type T, or null if not found or an error occurred.
 */
suspend inline fun <reified T : Any> BaseRepository.fetchSingle(
    column: String,
    value: Any
): T? {
    return try {
        client.postgrest
            .from(tableName)
            .select { filter { eq(column, value) } }
            .decodeSingle<T>()
    } catch (e: Exception) {
        Log.e(tag, "Error fetching single from $tableName where $column = $value", e)
        null
    }
}

/**
 * Fetches a list of records from the database based on a column-value pair.
 * 
 * @param column The name of the column to filter by.
 * @param value The value the column must match.
 * @return A list of decoded objects of type T. Returns an empty list on failure.
 */
suspend inline fun <reified T : Any> BaseRepository.fetchList(
    column: String,
    value: Any
): List<T> {
    return try {
        client.postgrest
            .from(tableName)
            .select { filter { eq(column, value) } }
            .decodeList<T>()
    } catch (e: Exception) {
        Log.e(tag, "Error fetching list from $tableName where $column = $value", e)
        emptyList()
    }
}

/**
 * Fetches all records from the repository's table.
 * 
 * @return A list of all decoded objects of type T in the table.
 */
suspend inline fun <reified T : Any> BaseRepository.fetchAll(): List<T> {
    return try {
        client.postgrest
            .from(tableName)
            .select()
            .decodeList<T>()
    } catch (e: Exception) {
        Log.e(tag, "Error fetching all from $tableName", e)
        emptyList()
    }
}

/**
 * Inserts a new record into the database and returns the result.
 * 
 * @param item The object to insert.
 * @return The inserted record as returned by the database, or null on failure.
 */
suspend inline fun <reified TInsert : Any, reified TResult : Any> BaseRepository.insert(
    item: TInsert
): TResult? {
    return try {
        client.postgrest
            .from(tableName)
            .insert(item) { select() }
            .decodeSingle<TResult>()
    } catch (e: Exception) {
        Log.e(tag, "Error inserting into $tableName", e)
        null
    }
}

/**
 * Updates an existing record in the database.
 * 
 * @param column The column to filter by (usually 'id').
 * @param value The value to match for the update.
 * @param item The new data for the record.
 * @return The updated record, or null on failure.
 */
suspend inline fun <reified T : Any> BaseRepository.update(
    column: String,
    value: Any,
    item: T
): T? {
    return try {
        client.postgrest
            .from(tableName)
            .update(item) {
                select()
                filter { eq(column, value) }
            }
            .decodeSingle<T>()
    } catch (e: Exception) {
        Log.e(tag, "Error updating $tableName where $column = $value", e)
        null
    }
}

/**
 * Deletes a record from the database.
 * 
 * @param column The column to filter by.
 * @param value The value to match for deletion.
 * @return True if the operation was successful, false otherwise.
 */
suspend inline fun <reified T : Any> BaseRepository.delete(
    column: String,
    value: Any
): Boolean {
    return try {
        client.postgrest
            .from(tableName)
            .delete { filter { eq(column, value) } }
        true
    } catch (e: Exception) {
        Log.e(tag, "Error deleting from $tableName where $column = $value", e)
        false
    }
}

/**
 * Utility function to fetch records from a specific table with custom column selection.
 */
suspend inline fun <reified T : Any> BaseRepository.fetchTableList(
    targetTable: String = tableName,
    columns: Columns = Columns.ALL,
    column: String,
    value: Any
): List<T> {
    return try {
        client.postgrest
            .from(targetTable)
            .select(columns) {
                filter { eq(column, value) }
            }
            .decodeList<T>()
    } catch (e: Exception) {
        Log.e(tag, "Error fetching list from $targetTable where $column = $value", e)
        emptyList()
    }
}
