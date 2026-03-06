package com.group5.gue.api

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest

object BaseRepository {

    @PublishedApi
    internal val client = SupabaseProvider.supabaseClient
    const val TAG = "BaseRepository"

    suspend inline fun <reified T : Any> fetchSingle(
        table: String,
        column: String,
        value: Any
    ): T? {
        return try {
            client.postgrest
                .from(table)
                .select { filter { eq(column, value) } }
                .decodeSingle<T>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching single from $table where $column = $value", e)
            null
        }
    }

    suspend inline fun <reified T : Any> fetchList(
        table: String,
        column: String,
        value: Any
    ): List<T> {
        return try {
            client.postgrest
                .from(table)
                .select { filter { eq(column, value) } }
                .decodeList<T>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching list from $table where $column = $value", e)
            emptyList()
        }
    }

    suspend inline fun <reified T : Any> fetchAll(
        table: String
    ): List<T> {
        return try {
            client.postgrest
                .from(table)
                .select()
                .decodeList<T>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all from $table", e)
            emptyList()
        }
    }

    suspend inline fun <reified T : Any> insert(
        table: String,
        item: T
    ): T? {
        return try {
            client.postgrest
                .from(table)
                .insert(item) { select() }
                .decodeSingle<T>()
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting into $table", e)
            null
        }
    }

    suspend inline fun <reified T : Any> update(
        table: String,
        column: String,
        value: Any,
        item: T
    ): T? {
        return try {
            client.postgrest
                .from(table)
                .update(item) {
                    select()
                    filter { eq(column, value) }
                }
                .decodeSingle<T>()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating $table where $column = $value", e)
            null
        }
    }

    suspend inline fun <reified T : Any> delete(
        table: String,
        column: String,
        value: Any
    ): Boolean {
        return try {
            client.postgrest
                .from(table)
                .delete { filter { eq(column, value) } }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting from $table where $column = $value", e)
            false
        }
    }
}