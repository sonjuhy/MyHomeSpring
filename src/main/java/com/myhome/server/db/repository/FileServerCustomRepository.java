package com.myhome.server.db.repository;

import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.api.dto.FileServerPublicTrashDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FileServerCustomRepository {
    private final JdbcTemplate jdbcTemplate;
    private final int batchSize = 500000;
    @Transactional
    public void saveBatchPublic(List<FileServerPublicDto> list){
        jdbcTemplate.execute("DELETE FROM FILE_PUBLIC_TB");
        jdbcTemplate.execute("ALTER TABLE FILE_PUBLIC_TB AUTO_INCREMENT=1");
        int batchCount = 0;
        List<FileServerPublicDto> subList;
        for(int i=0;i<list.size();i=batchCount*batchSize){
            int end;
            batchCount++;
            if(batchCount*batchSize >= list.size()) end = list.size();
            else end = batchCount*batchSize;

            subList = new ArrayList<>(list.subList(i, end));
            batchPublicInsert(subList);
        }
    }

    @Transactional
    public void saveBatchPrivate(List<FileServerPrivateDto> list){
        jdbcTemplate.execute("DELETE FROM FILE_PRIVATE_TB");
        jdbcTemplate.execute("ALTER TABLE FILE_PRIVATE_TB AUTO_INCREMENT=1");
        int batchCount = 0;
        List<FileServerPrivateDto> subList;
        for(int i=0;i<list.size();i=batchCount*batchSize){
            int end;
            batchCount++;
            if(batchCount*batchSize >= list.size()) end = list.size();
            else end = batchCount*batchSize;

            subList = new ArrayList<>(list.subList(i, end));
            batchPrivateInsert(subList);
        }
    }
    private void batchPublicInsert(List<FileServerPublicDto> list){
        if(list.isEmpty()) return;
        jdbcTemplate.batchUpdate("INSERT INTO " +
                "FILE_PUBLIC_TB(UUID_CHAR, PATH_CHAR, NAME_CHAR, TYPE_CHAR, SIZE_FLOAT, LOCATION_CHAR, STATE_INT, DELETE_STATUS_INT) " +
                "value(?, ?, ?, ?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, list.get(i).getUuidName());
                ps.setString(2, list.get(i).getPath());
                ps.setString(3, list.get(i).getName());
                ps.setString(4, list.get(i).getType());
                ps.setString(5, String.valueOf(list.get(i).getSize()));
                ps.setString(6, list.get(i).getLocation());
                ps.setString(7, String.valueOf(list.get(i).getState()));
                ps.setString(8, String.valueOf(list.get(i).getDeleteStatus()));
            }

            @Override
            public int getBatchSize() {
                return list.size();
            }
        });
        list.clear();
    }
    private void batchPrivateInsert(List<FileServerPrivateDto> list){
        jdbcTemplate.batchUpdate("INSERT INTO " +
                "FILE_PRIVATE_TB(UUID_CHAR, PATH_CHAR, NAME_CHAR, TYPE_CHAR, SIZE_FLOAT, OWNER_CHAR, LOCATION_CHAR, STATE_INT, DELETE_STATUS_INT) " +
                "value(?, ?, ?, ?, ?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, list.get(i).getUuidName());
                ps.setString(2, list.get(i).getPath());
                ps.setString(3, list.get(i).getName());
                ps.setString(4, list.get(i).getType());
                ps.setString(5, String.valueOf(list.get(i).getSize()));
                ps.setString(6, list.get(i).getOwner());
                ps.setString(7, list.get(i).getLocation());
                ps.setString(8, String.valueOf(list.get(i).getState()));
                ps.setString(9, String.valueOf(list.get(i).getDelete()));
            }

            @Override
            public int getBatchSize() {
                return list.size();
            }
        });

        list.clear();
    }
}
