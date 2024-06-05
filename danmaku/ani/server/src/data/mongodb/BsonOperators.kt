@file:Suppress(
    "NOTHING_TO_INLINE",
    "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE" // 让 `value` 必须兼容 property 类型
)

package me.him188.ani.danmaku.server.data.mongodb

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.BsonField
import com.mongodb.client.model.CountOptions
import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndDeleteOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.bson.conversions.Bson
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


interface BsonExtensions {
    infix fun Bson.and(other: Bson?): Bson {
        if (other == null) return this
        return Filters.and(this, other)
    }

    infix fun Bson.or(other: Bson): Bson {
        return Filters.or(this, other)
    }
}

open class BsonUpdateScope : BsonExtensions {
    @PublishedApi
    internal val sets: MutableList<Bson> = mutableListOf()

    // inline property to get compiler optimization

    inline fun <T> KProperty<List<T>>.add(value: @kotlin.internal.NoInfer T): List<Bson> {
        sets.add(Updates.addToSet(this.name, value))
        return sets
    }

    inline fun <T> set(name: KProperty<T>, value: @kotlin.internal.NoInfer T): List<Bson> {
        sets.add(Updates.set(name.name, value))
        return sets
    }

    inline fun <T> setOnInsert(name: KProperty<T>, value: @kotlin.internal.NoInfer T): List<Bson> {
        sets.add(Updates.setOnInsert(name.name, value))
        return sets
    }

    inline infix fun <T> KProperty<T>.setTo(value: @kotlin.internal.NoInfer T): List<Bson> {
        sets.add(Updates.set(name, value))
        return sets
    }

    inline operator fun <T : Number> KProperty<T>.plusAssign(value: @kotlin.internal.NoInfer T) {
        sets.add(Updates.inc(name, value))
    }

    inline operator fun KProperty<Int>.minusAssign(value: Int) {
        sets.add(Updates.inc(name, -value))
    }

    inline operator fun KProperty<Long>.minusAssign(value: Long) {
        sets.add(Updates.inc(name, -value))
    }

    inline operator fun KProperty<Float>.minusAssign(value: Float) {
        sets.add(Updates.inc(name, -value))
    }

    inline operator fun KProperty<Double>.minusAssign(value: Double) {
        sets.add(Updates.inc(name, -value))
    }

    inline fun <T> setIfNotNull(name: KProperty<T>, value: @kotlin.internal.NoInfer T?): List<Bson> {
        value ?: return sets
        sets.add(Updates.set(name.name, value))
        return sets
    }
}

interface BsonFilterScope : BsonExtensions {
    infix fun KProperty<String?>.matches(regex: String): Bson {
        return Filters.regex(this.name, regex)
    }

    infix fun <TItem> KProperty<TItem>.eq(value: @kotlin.internal.NoInfer TItem?): Bson {
        return Filters.eq(this.name, value)
    }

    infix fun <TItem> KProperty<TItem>.`in`(values: Iterable<@kotlin.internal.NoInfer TItem>): Bson {
        return Filters.`in`(this.name, values)
    }

    infix fun <TItem : Comparable<TItem>?> KProperty<TItem>.gt(value: @kotlin.internal.NoInfer TItem & Any): Bson {
        return Filters.gt(this.name, value)
    }

    infix fun <TItem : Comparable<TItem>?> KProperty<TItem>.gte(value: @kotlin.internal.NoInfer TItem & Any): Bson {
        return Filters.gte(this.name, value)
    }

    infix fun <TItem : Comparable<TItem>?> KProperty<TItem>.lt(value: @kotlin.internal.NoInfer TItem & Any): Bson {
        return Filters.lt(this.name, value)
    }

    infix fun <TItem : Comparable<TItem>?> KProperty<TItem>.lte(value: @kotlin.internal.NoInfer TItem & Any): Bson {
        return Filters.lte(this.name, value)
    }
}

interface BsonSortScope : BsonExtensions {

    fun ascending(vararg names: String): Bson = Sorts.ascending(*names)

    fun ascending(vararg names: KProperty<*>): Bson {
        return ascending(*names.map { it.name }.toTypedArray())
    }

    fun descending(vararg names: String): Bson {
        return Sorts.descending(*names)
    }

    fun descending(vararg names: KProperty<*>): Bson {
        return descending(*names.map { it.name }.toTypedArray())
    }
}

class BsonScope : BsonUpdateScope(), BsonFilterScope, BsonSortScope

inline fun <R> bson(block: BsonScope.() -> R): R =
    BsonScope().block()

inline fun bsonUpdate(block: BsonScope.() -> Unit): List<Bson> = BsonScope().apply(block).sets


inline fun <T : Any> FindFlow<T>.sortBy(block: BsonSortScope.() -> Bson): FindFlow<T> =
    sort(BsonScope().block())

inline fun FindOneAndUpdateOptions.sortBy(block: BsonSortScope.() -> Bson) =
    sort(BsonScope().block())

inline fun <T : Any> MongoCollection<T>.findBy(block: BsonFilterScope.() -> Bson): FindFlow<T> =
    find(BsonScope().block())

class BsonAggregateScope {
    val operations = mutableListOf<Bson>()

    class MatchScope : BsonFilterScope

    inline fun match(block: MatchScope.() -> Bson) {
        operations.add(Aggregates.match(MatchScope().run(block)))
    }

    class GroupScope {
        val fields = mutableListOf<BsonField>()

        fun sum(name: String, expression: String) {
            fields.add(Accumulators.sum(name, expression))
        }

        fun sum(name: String, prop: KProperty<*>) {
            fields.add(Accumulators.sum(name, "$" + prop.name))
        }

        /**
         * @see Accumulators
         */
        fun field(bson: BsonField) {
            fields.add(bson)
        }
    }

    inline fun group(
        id: Any? = null,
        block: GroupScope.() -> Unit,
    ) {
        operations.add(Aggregates.group(id, GroupScope().apply(block).fields))
    }

    @JvmInline
    value class ProjectionScope(
        val fields: MutableList<Bson> = mutableListOf(),
    ) {
        inline fun include(prop: KProperty<*>) {
            fields.add(Projections.include(prop.name))
        }

        inline fun exclude(prop: KProperty<*>) {
            fields.add(Projections.exclude(prop.name))
        }

        inline fun excludeId() {
            fields.add(Projections.excludeId())
        }
    }

    inline fun project(
        block: ProjectionScope.() -> Unit,
    ) {
        operations.add(
            Aggregates.project(
                Projections.fields(ProjectionScope().apply(block).fields)
            )
        )
    }
}

inline fun <T : Any> MongoCollection<T>.aggregateBy(block: BsonAggregateScope.() -> Unit): AggregateFlow<T> =
    aggregate(BsonAggregateScope().apply(block).operations)

/**
 * @param R 返回类型
 */
inline fun <R : Any> MongoCollection<*>.aggregateByTo(
    resultClass: KClass<R>,
    block: BsonAggregateScope.() -> Unit,
): AggregateFlow<R> =
    aggregate(BsonAggregateScope().apply(block).operations, resultClass.java)

/**
 * @param R 返回类型
 */
inline fun <reified R : Any> MongoCollection<*>.aggregateByTo(
    block: BsonAggregateScope.() -> Unit,
): AggregateFlow<R> =
    aggregate(BsonAggregateScope().apply(block).operations, R::class.java)


suspend inline fun <T : Any> MongoCollection<T>.updateOneBy(
    filter: BsonFilterScope.() -> Bson,
    update: BsonUpdateScope.() -> Unit,
    options: UpdateOptions.() -> Unit = {},
): UpdateResult {
    val list = bsonUpdate(update)
    return this.updateOne(bson(filter), Updates.combine(list), UpdateOptions().apply(options))
}

suspend inline fun <T : Any> MongoCollection<T>.updateOneById(
    id: String,
    update: BsonUpdateScope.() -> Unit,
    options: UpdateOptions.() -> Unit = {},
): UpdateResult = updateOneBy({ Filters.eq("_id", id) }, update, options)

suspend inline fun <T : Any> MongoCollection<T>.updateManyBy(
    filter: BsonFilterScope.() -> Bson,
    update: BsonUpdateScope.() -> Unit,
    options: UpdateOptions.() -> Unit = {},
): UpdateResult {
    val list = bsonUpdate(update)
    return this.updateMany(bson(filter), Updates.combine(list), UpdateOptions().apply(options))
}

suspend inline fun <T : Any> MongoCollection<T>.updateManyById(
    id: String,
    update: BsonUpdateScope.() -> Unit,
    options: UpdateOptions.() -> Unit = {},
): UpdateResult = updateManyBy({ Filters.eq("_id", id) }, update, options)

suspend inline fun <T : Any> MongoCollection<T>.findOneAndUpdateBy(
    filter: BsonFilterScope.() -> Bson,
    update: BsonUpdateScope.() -> Unit,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions(),
): T? {
    val list = bsonUpdate(update)
    return this.findOneAndUpdate(bson(filter), Updates.combine(list), options)
}

suspend inline fun <T : Any> MongoCollection<T>.findOneAndUpdateBy(
    filter: BsonFilterScope.() -> Bson,
    update: BsonUpdateScope.() -> Unit,
    options: FindOneAndUpdateOptions.() -> Unit = {},
): T? = findOneAndUpdateBy(filter, update, FindOneAndUpdateOptions().apply(options))

suspend inline fun <T : Any> MongoCollection<T>.findOneAndDeleteBy(
    options: FindOneAndDeleteOptions = FindOneAndDeleteOptions(),
    filter: BsonFilterScope.() -> Bson,
): T? = this.findOneAndDelete(bson(filter), options)

suspend inline fun <T : Any> MongoCollection<T>.deleteOneBy(
    options: DeleteOptions = DeleteOptions(),
    filter: BsonFilterScope.() -> Bson,
): DeleteResult = this.deleteOne(bson(filter), options)

suspend inline fun <T : Any> MongoCollection<T>.deleteOneById(
    id: String?,
    options: DeleteOptions = DeleteOptions(),
): DeleteResult = this.deleteOneBy(options) { Filters.eq("_id", id) }

suspend inline fun <T : Any> MongoCollection<T>.deleteManyBy(
    options: DeleteOptions = DeleteOptions(),
    filter: BsonFilterScope.() -> Bson,
): DeleteResult = this.deleteMany(bson(filter), options)

suspend inline fun <T : Any> MongoCollection<T>.countBy(
    options: CountOptions = CountOptions(),
    filter: BsonFilterScope.() -> Bson,
): Long = this.countDocuments(bson(filter), options)
