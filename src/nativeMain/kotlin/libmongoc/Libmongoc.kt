package libmongoc

import kotlinx.cinterop.*
import kotlinx.serialization.json.*

object Libmongoc {

    private val cleanupJson = buildJsonObject { put("ok", 0.0) }
    private val appName = "knMongoc"

    fun isEmptyJsonArray(jsonArray : JsonArray) = jsonArray.toString() == "[]"

    private fun jsonToBson(jsonObject : JsonObject): CPointer<bson_t>? {
        return get_bson_from_json(jsonObject.toString().cstr)
    }

    private fun bsonToJsonObject(bsonObjectPtr : CValuesRef<bson_t>): JsonObject {
        return Json.parseToJsonElement(bsonToJsonString(bsonObjectPtr)).jsonObject
    }

    private fun bsonToJsonString(bsonObjectPtr : CValuesRef<bson_t>): String {
        return bson_as_json(bsonObjectPtr, null)!!.toKString()
    }

    private fun bsonErrorToString(bsonErrorObject : bson_error_t): String {
        return bsonErrorObject.message.toKString()
    }

    private fun connect() : CPointer<mongoc_client_t> = memScoped {
        val mongoUriString = "mongodb://localhost:27017"
        val mongoError = alloc<bson_error_t>()

        mongoc_init()

        val mongoUri = mongoc_uri_new_with_error(mongoUriString, mongoError.ptr) ?: throw Exception("mongoc_uri_new_with_error($mongoUriString) failed:" + bsonErrorToString(mongoError))

        val mongoClient = mongoc_client_new_from_uri(mongoUri) ?: throw Exception("mongoc_client_new_from_uri($mongoUriString) failed:")

        if (!mongoc_client_set_appname(mongoClient, appName)) throw Exception("mongoc_client_set_appname($appName) failed")

        return mongoClient
    }

    fun ping() : JsonObject = memScoped {
        val mongoError = alloc<bson_error_t>()
        val mongoReply = alloc<bson_t>()
        val pingCommand = buildJsonObject { put("ping", 1) }

        val mongoClient = connect()

        if (!mongoc_client_command_simple(mongoClient, "admin", jsonToBson(pingCommand), null, mongoReply.ptr, mongoError.ptr)) throw Exception("mongoc_client_command_simple(${pingCommand}) failed:" + bsonErrorToString(mongoError))

        cleanup(mongoClient)

        return bsonToJsonObject(mongoReply.ptr)
    }

    fun insertOne(dbName: String, collectionName: String, jsonObject : JsonObject) : JsonObject = memScoped {
        val mongoError = alloc<bson_error_t>()
        val mongoReply = alloc<bson_t>()

        val mongoClient = connect()

        val collection = mongoc_client_get_collection(mongoClient, dbName, collectionName);

        if (!mongoc_collection_insert_one(collection, jsonToBson(jsonObject), null, mongoReply.ptr, mongoError.ptr)) throw Exception("mongoc_collection_insert_one($dbName, $collectionName, ${jsonObject}) failed:" + bsonErrorToString(mongoError))

        cleanup(mongoClient)

        return bsonToJsonObject(mongoReply.ptr)
    }

    fun findByFilter(dbName: String, collectionName: String, jsonFilter : JsonObject) : JsonArray = memScoped {
        val mongoDoc = alloc<CPointerVar<bson_t>>()

        val mongoClient = connect()

        val collection = mongoc_client_get_collection(mongoClient, dbName, collectionName);

        val queryCursor = mongoc_collection_find_with_opts(collection, jsonToBson(jsonFilter), null, null) ?: throw Exception("mongoc_collection_find_with_opts($dbName, $collectionName, ${jsonFilter}) failed")

        val returnJsonArray = buildJsonArray {
            while (mongoc_cursor_next(queryCursor, mongoDoc.ptr)) {
                add(bsonToJsonObject(mongoDoc.value!!))
            }
        }
        cleanup(mongoClient)

        return returnJsonArray
    }

    fun updateBySelector(dbName: String, collectionName: String, jsonSelector : JsonObject, jsonReplaceObject : JsonObject) : JsonObject = memScoped {
        val mongoError = alloc<bson_error_t>()
        val mongoReply = alloc<bson_t>()
        val replaceCommand = buildJsonObject { put("\$set", jsonReplaceObject) }

        val mongoClient = connect()

        val collection = mongoc_client_get_collection(mongoClient, dbName, collectionName);

        if (!mongoc_collection_update_one(collection, jsonToBson(jsonSelector), jsonToBson(replaceCommand), null, mongoReply.ptr, mongoError.ptr)) throw Exception("mongoc_collection_update_one($dbName, $collectionName, ${jsonSelector}, ${jsonReplaceObject}) failed :" + bsonErrorToString(mongoError))

        cleanup(mongoClient)

        return bsonToJsonObject(mongoReply.ptr)
    }

    fun deleteBySelector(dbName: String, collectionName: String, jsonSelector : JsonObject) : JsonObject = memScoped {
        val mongoError = alloc<bson_error_t>()
        val mongoReply = alloc<bson_t>()

        val mongoClient = connect()

        val collection = mongoc_client_get_collection(mongoClient, dbName, collectionName);

        if (!mongoc_collection_delete_one(collection, jsonToBson(jsonSelector),null, mongoReply.ptr, mongoError.ptr)) throw Exception("mongoc_collection_delete_one($dbName, $collectionName, ${jsonSelector}) failed :" + bsonErrorToString(mongoError))

        cleanup(mongoClient)

        return bsonToJsonObject(mongoReply.ptr)
    }

    private fun cleanup(mongoClient : CPointer<mongoc_client_t>) : JsonObject {
        mongoc_client_destroy(mongoClient)
        mongoc_cleanup()

        return cleanupJson
    }

}