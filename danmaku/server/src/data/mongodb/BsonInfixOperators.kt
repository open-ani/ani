package me.him188.ani.danmaku.server.data.mongodb

import com.mongodb.client.model.Filters
import org.bson.conversions.Bson

infix fun Bson.and(other: Bson): Bson {
    return Filters.and(this, other)
}

infix fun Bson.or(other: Bson): Bson {
    return Filters.or(this, other)
}

data class Field(val name: String) {
    infix fun <TItem> eq(value: TItem): Bson {
        return Filters.eq(name, value)
    }

    infix fun <TItem> gt(value: TItem & Any): Bson {
        return Filters.gt(name, value)
    }

    infix fun <TItem> gte(value: TItem & Any): Bson {
        return Filters.gte(name, value)
    }

    infix fun <TItem> lt(value: TItem & Any): Bson {
        return Filters.lt(name, value)
    }

    infix fun <TItem> lte(value: TItem & Any): Bson {
        return Filters.lte(name, value)
    }
}