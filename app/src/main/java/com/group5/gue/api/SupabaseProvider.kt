package com.group5.gue.api

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Singleton provider for the Supabase client.
 * This object initializes and holds a lazy reference to the SupabaseClient instance,
 * configuring it with the necessary modules such as Auth, Postgrest, and Storage.
 */
object SupabaseProvider {
    
    /** 
     * The base URL of the Supabase project.
     * Note: This should ideally be moved to local.properties or a secure configuration file.
     */
    private const val SUPABASE_URL = "https://wvcpjygatizwlusdgwno.supabase.co"
    
    /** 
     * The public anon key used to authenticate requests to the Supabase API.
     * Note: This should ideally be moved to local.properties or a secure configuration file.
     */
    private const val SUPABASE_KEY = "sb_publishable_p5UsVza2oMOi-P2jGDX3xg_7GtMduWT"

    /**
     * The lazily initialized Supabase client.
     * This client is shared across the entire application and is pre-configured
     * with Auth (for user management), Postgrest (for database queries), and Storage (for file management).
     */
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