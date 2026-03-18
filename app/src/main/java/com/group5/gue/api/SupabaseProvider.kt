package com.group5.gue.api

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {
    //Todo: Move these to a local.properties
    private const val SUPABASE_URL = "https://wvcpjygatizwlusdgwno.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_p5UsVza2oMOi-P2jGDX3xg_7GtMduWT"

    val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}