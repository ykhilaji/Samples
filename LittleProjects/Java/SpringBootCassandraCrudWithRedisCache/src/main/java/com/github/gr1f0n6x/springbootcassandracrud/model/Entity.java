package com.github.gr1f0n6x.springbootcassandracrud.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Table(value = "entity")
public class Entity implements Serializable {
    @PrimaryKey
    private Key key;
    @Column
    private String value;

    public Entity() {
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(key, entity.key) &&
                Objects.equals(value, entity.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "key=" + key +
                ", value='" + value + '\'' +
                '}';
    }

    @PrimaryKeyClass
    public static class Key implements Serializable {
        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1, ordering = Ordering.ASCENDING)
        private long id;
        @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2, ordering = Ordering.DESCENDING)
        private String type;
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 3, ordering = Ordering.ASCENDING)
        private LocalDateTime time;

        public Key() {
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public void setTime(LocalDateTime time) {
            this.time = time;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "id=" + id +
                    ", type='" + type + '\'' +
                    ", time=" + time +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return id == key.id &&
                    Objects.equals(type, key.type) &&
                    Objects.equals(time, key.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, type, time);
        }
    }
}
