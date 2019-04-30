package org.kumoricon.registration.model.order;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {
    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public Order findById(Integer id) {
        try {
            return jdbcTemplate.queryForObject(
                    //"select orders.id, orders.notes, orders.order_id, users.username as order_taken_by_user, orders.paid from orders join users on orders.order_taken_by_user=users.id where orders.id=?",
                    "select * from orders where id=?",
                    new Object[]{id}, new OrderRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    @Transactional(readOnly = true)
    public Order findByOrderNumber(String orderId) {
        try {
            return jdbcTemplate.queryForObject(
                    "select * from orders where order_id=?",
                    new Object[]{orderId}, new OrderRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalByOrderNumber(String orderId) {
        try {
            return jdbcTemplate.queryForObject(
                    "select sum(attendees.paid_amount) from attendees join orders o on attendees.order_id = o.id\n" +
                            "where o.order_id = ?",
                    new Object[]{orderId}, BigDecimal.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    @Transactional(readOnly = true)
    public List<Order> findAllBy(Integer page) {
        return jdbcTemplate.query("select * from orders ORDER BY id desc LIMIT ? OFFSET ?",
                new Object[]{20, 20*page},
                new OrderRowMapper());
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        try {
            return jdbcTemplate.query("select * from orders",
                    new OrderRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Transactional
    public void saveAll(List<Order> orders) {
        for (Order order : orders) {
            save(order);
        }
    }

    @Transactional(readOnly = true)
    public Integer count() {
        String sql = "select count(*) from orders";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Transactional
    public Integer save(Order order) {
        if (order.getId() == null) {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("orders").usingGeneratedKeyColumns("id");
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("notes", order.getNotes());
            parameters.put("order_id", order.getOrderId());
            parameters.put("order_taken_by_user", order.getOrderTakenByUser());
            parameters.put("paid", order.getPaid());
            Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
            return (key).intValue();
        } else {
            jdbcTemplate.update("UPDATE orders SET notes = ?, order_id = ?, order_taken_by_user = ?, paid =? WHERE id = ?",
                    order.getNotes(), order.getOrderId(), order.getOrderTakenByUser(), order.getPaid(), order.getId());
            return order.getId();
        }
    }


    private class OrderRowMapper implements RowMapper<Order>
    {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order o = new Order();
            o.setId(rs.getInt("id"));
            o.setOrderId(rs.getString("order_id"));
            o.setOrderTakenByUser(rs.getInt("order_taken_by_user"));
            o.setPaid(rs.getBoolean("paid"));
            return o;
        }
    }

}