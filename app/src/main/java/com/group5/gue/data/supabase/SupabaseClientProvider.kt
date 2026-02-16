package com.group5.gue.data.supabase

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

private const val SUPABASE_URL = "https://wvcpjygatizwlusdgwno.supabase.co"
private const val SUPABASE_KEY = "sb_publishable_p5UsVza2oMOi-P2jGDX3xg_7GtMduWT"

object SupabaseClientProvider {
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}
