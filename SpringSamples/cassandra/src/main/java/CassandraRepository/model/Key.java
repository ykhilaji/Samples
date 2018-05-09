package CassandraRepository.model;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;

@PrimaryKeyClass
public class Key implements Serializable {
    @PrimaryKeyColumn(name = "row_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED, ordering = Ordering.ASCENDING)
    private Long rowId;
    @PrimaryKeyColumn(name = "cluster_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private String clusterId;

    public Key() {
    }

    public Key(Long rowId, String clusterId) {
        this.rowId = rowId;
        this.clusterId = clusterId;
    }

    public Long getRowId() {
        return rowId;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (rowId != null ? !rowId.equals(key.rowId) : key.rowId != null) return false;
        return clusterId != null ? clusterId.equals(key.clusterId) : key.clusterId == null;
    }

    @Override
    public int hashCode() {
        int result = rowId != null ? rowId.hashCode() : 0;
        result = 31 * result + (clusterId != null ? clusterId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Key{" +
                "rowId=" + rowId +
                ", clusterId='" + clusterId + '\'' +
                '}';
    }
}
