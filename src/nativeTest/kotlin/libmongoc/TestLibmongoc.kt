package libmongoc

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.*
import profile.EarthLocation
import profile.Profile
import kotlin.test.Test
import kotlin.test.assertTrue

class TestLibmongoc {

    @Test
    fun testConnection() {
        println(Libmongoc.ping())
    }

    @ExperimentalUnsignedTypes
    @Test
    fun testInsert() {

        println(Libmongoc.insertOne("testInsert", "testInsertEarthLocation"
            , Json.encodeToJsonElement(SerializableEarthLocation(getTestEarthLocation())).jsonObject)
        )

        println(Libmongoc.insertOne("testInsert", "testInsertProfile"
            , Json.encodeToJsonElement(SerializableProfile(getTestProfile())).jsonObject)
        )
    }

    @ExperimentalUnsignedTypes
    @Test
    fun testFindTwice() {
        //find by Data
        val findByDataFilter = buildJsonObject { put("longitude", getTestEarthLocation().longitude) }

        val foundByDataJsonArray = Libmongoc.findByFilter("testInsert", "testInsertEarthLocation", findByDataFilter)

        println("by-data serialized: $foundByDataJsonArray")

        val foundByDataSerializableEarthLocationArray : Array<SerializableEarthLocation> = Json { ignoreUnknownKeys = true }.decodeFromJsonElement(foundByDataJsonArray)

        val firstByDataFoundSerializable = foundByDataSerializableEarthLocationArray[0]

        println("by-data deserialized(${foundByDataSerializableEarthLocationArray.size})[0]: " + firstByDataFoundSerializable)

        val foundByDataEarthLocation = EarthLocation(foundByDataSerializableEarthLocationArray[0])

        println("by-data reconstructed[0]: $foundByDataEarthLocation")

        //find by Id (same method)
        val findByIdFilter = buildJsonObject { put("uuid", "e3fab7e2-887e-4996-a9bc-f894181ff315") } //uuid found via mongo client, "db.testInsertProfile.find()"

        val foundByIdJsonArray = Libmongoc.findByFilter("testInsert", "testInsertProfile", findByIdFilter)

        println("by-id serialized: $foundByIdJsonArray")

        val foundByIdSerializableProfileArray : Array<SerializableProfile> = Json { ignoreUnknownKeys = true }.decodeFromJsonElement(foundByIdJsonArray)

        val firstByIdFoundSerializable = foundByIdSerializableProfileArray[0]

        println("by-id deserialized(${foundByIdSerializableProfileArray.size})[0]: " + firstByIdFoundSerializable)

        val foundByIdProfile = Profile(foundByIdSerializableProfileArray[0])

        println("by-id reconstructed[0]: $foundByIdProfile")

    }

    @ExperimentalUnsignedTypes
    @Test
    fun testUpdate() {

        val findFilter = buildJsonObject { put("uuid", "35ebd0c9-739c-434c-91c6-f117ebcb6e45") } //uuid found via mongo client, "db.testInsertEarthLocation.find()"

        val foundJsonArray = Libmongoc.findByFilter("testInsert", "testInsertEarthLocation", findFilter)

        println("serialized: $foundJsonArray")

        val foundSerializableEarthLocationArray : Array<SerializableEarthLocation> = Json { ignoreUnknownKeys = true }.decodeFromJsonElement(foundJsonArray)

        val firstFoundSerializable = foundSerializableEarthLocationArray[0]

        println("deserialized(${foundSerializableEarthLocationArray.size})[0]: " + firstFoundSerializable)

        val updatedSerializableEarthLocation = SerializableEarthLocation(
                EarthLocation(foundSerializableEarthLocationArray[0])
                , updUuId = firstFoundSerializable.uuid
                , updLongitude = LON_ABQ
                , updLatitude = LAT_ABQ
                , updAltitude = ALT_ABQ
                , updTimeZoneStr = TZ_ABQ.toString()
                , updUtcDateTimeStr = Clock.System.now().toString())

        println("updated deserialized: $updatedSerializableEarthLocation")

        val updatedSerializedEarthLocation = Json.encodeToJsonElement(updatedSerializableEarthLocation).jsonObject

        println("updated serialized: $updatedSerializedEarthLocation")

        println(Libmongoc.updateBySelector("testInsert", "testInsertEarthLocation", buildJsonObject { put("uuid", firstFoundSerializable.uuid) }, updatedSerializedEarthLocation))
    }

    @ExperimentalUnsignedTypes
    @Test
    fun testDelete() {

        val deleteSelector = buildJsonObject { put("uuid", "e3fab7e2-887e-4996-a9bc-f894181ff315") } //uuid found via mongo client, "db.testInsertProfile.find()"

        println(Libmongoc.deleteBySelector("testInsert", "testInsertProfile", deleteSelector))

        val foundById = Libmongoc.findByFilter("testInsert", "testInsertProfile", buildJsonObject { put("uuid", "e3fab7e2-887e-4996-a9bc-f894181ff315") })

        println(foundById)

        assertTrue(foundById.isEmpty())
    }

    companion object {
        private val LON_ATX = (-97.7431)
        private val LAT_ATX = 30.2672
        private val ALT_ATX = ((130 + 305) / 2).toDouble()//avg elevation
        private val TZ_ATX = TimeZone.of("America/Chicago")

        private val LON_ABQ = (-106.65)
        private val LAT_ABQ = 35.0833
        private val ALT_ABQ = 1510.00 //timeanddate.com use this
        private val TZ_ABQ = TimeZone.of("America/Denver")

        private fun getTestEarthLocation() = EarthLocation(
            longitude = LON_ATX
            , latitude = LAT_ATX
            , altitude = ALT_ATX
            , timeZone = TZ_ATX)

        private fun getTestProfile() = Profile("ATX", getTestEarthLocation())

    }
}