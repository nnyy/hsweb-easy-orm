package org.hswebframework.ezorm.rdb.metadata;

import lombok.*;
import org.hswebframework.ezorm.core.meta.AbstractColumnMetadata;
import org.hswebframework.ezorm.core.meta.ColumnMetadata;
import org.hswebframework.ezorm.core.meta.Feature;
import org.hswebframework.ezorm.core.meta.ObjectType;
import org.hswebframework.ezorm.rdb.metadata.dialect.Dialect;

import java.sql.JDBCType;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RDBColumnMetadata extends AbstractColumnMetadata implements ColumnMetadata, Cloneable, Comparable<RDBColumnMetadata> {

    /**
     * 数据类型,如:varchar(32)
     *
     * @since 1.0
     */
    private String dataType;

    /**
     * 长度
     *
     * @since 1.1
     */
    private int length;

    /**
     * 精度
     *
     * @since 1.1
     */
    private int precision;

    /**
     * 小数位数
     *
     * @since 1.1
     */
    private int scale;

    /**
     * 是否主键
     */
    private boolean primaryKey;

    /**
     * 自定义都列定义,使用它后其他列相关设置将无效
     *
     * @since 3.0
     */
    private String columnDefinition;

    /**
     * 是否可以更新
     *
     * @since 4.0
     */
    private boolean updatable = true;

    /**
     * JDBC Type
     */
    private JDBCType jdbcType;

    /**
     * 排序序号
     */
    private int sortIndex;

    /**
     * 所有者
     */
    private TableOrViewMetadata owner;

    /**
     * 曾经的名字
     */
    private String previousName;

    public Dialect getDialect() {
        return getOwner().getDialect();
    }

    public String getQuoteName() {
        return getDialect().quote(getName());
    }

    public String getDataType() {
        if (dataType == null) {
            return getDialect().buildDataType(this);
        }
        return dataType;
    }

    public String getPreviousName() {
        if (previousName == null) {
            previousName = name;
        }
        return previousName;
    }

    @Override
    public int compareTo(RDBColumnMetadata target) {
        return Integer.compare(sortIndex, target.getSortIndex());
    }

    @Override
    @SuppressWarnings("all")
    @SneakyThrows
    public RDBColumnMetadata clone() {
        RDBColumnMetadata columnMetadata = ((RDBColumnMetadata) super.clone());
        columnMetadata.setProperties(new HashMap<>(getProperties()));
        columnMetadata.setFeatures(new HashMap<>(getFeatures()));

        return columnMetadata;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", comment='" + comment + '\'' +
                ", dataType='" + dataType + '\'' +
                '}';
    }


    @Override
    public ObjectType getObjectType() {
        return RDBObjectType.column;
    }

    public <T extends Feature> Optional<T> findFeature(String id) {
        return of(this.<T>getFeature(id))
                .filter(Optional::isPresent)
                .orElseGet(() -> owner.findFeature(id));
    }

    public List<Feature> findFeatures(Predicate<Feature> predicate) {
        return Stream.concat(owner.findFeatures().stream(), getFeatureList().stream())
                .filter(predicate)
                .collect(Collectors.toList());

    }

    public String getFullName(String ownerName) {
        if (ownerName == null || ownerName.isEmpty()) {
            ownerName = getOwner().getName();
        }
        return getDialect().buildColumnFullName(ownerName, getName());
    }

    public String getFullName() {
        return getFullName(getOwner().getName());
    }

    public boolean isChanged(RDBColumnMetadata after) {

        return !this.getName().equals(this.getPreviousName())
                || this.getJdbcType() != after.getJdbcType()
                || (this.getDataType() != null && !this.getDataType().equals(after.getDataType()))
                || this.getLength() != after.getLength()
                || this.getScale() != after.getScale()
                || (this.getColumnDefinition() != null && !this.getColumnDefinition().equals(after.getColumnDefinition()));
    }


}