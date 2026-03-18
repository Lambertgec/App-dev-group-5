package com.group5.gue.api

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

interface BaseRepository {
    val tableName: String
    val client: SupabaseClient get() = SupabaseProvider.supabaseClient
    val tag: String get() = this::class.simpleName ?: "BaseRepository"
}

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
