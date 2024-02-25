package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerPublicEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FileServerPublicRepository extends JpaRepository<FileServerPublicEntity, Integer> {
//    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    FileServerPublicEntity findByPath(String path);

    FileServerPublicEntity findByUuid(String uuid);

    List<FileServerPublicEntity> findByLocationAndDelete(String location, int delete);
    List<FileServerPublicEntity> findByLocationAndDelete(String location, int delete, Pageable pageable);

    List<FileServerPublicEntity> findByState(int state);

    boolean existsByPath(String path);

    @Transactional
    int deleteByPath(String path);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM FILE_PUBLIC_TB WHERE STATE_INT=:state", nativeQuery = true)
    int deleteByState(@Param("state") int state);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE FILE_PUBLIC_TB SET LOCATION_CHAR=:location WHERE PATH_CHAR=:path", nativeQuery = true)
    int updateLocation(@Param("path") String path, @Param("location") String location);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE FILE_PUBLIC_TB SET STATE_INT=0", nativeQuery = true)
    int updateAllStateToZero();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE FILE_PUBLIC_TB SET STATE_INT=1", nativeQuery = true)
    int updateAllStateToOne();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INFO FILE_PUBLIC_TB(" +
            "UUID_PK, PATH_CHAR, NAME_CHAR, TYPE_CHAR, SIZE_FLOAT, LOCATION_CHAR, STATE_INT, DELETE_STATUS_INT) " +
            "SELECT :uuidPk :pathChar :nameChar :typeChar :sizeFloat :locationChar :stateInt :deleteStatusInt FROM DUAL WHERE NOT EXISTS(" +
            "SELECT FROM FILE_PUBLIC_TB WHERE UUID_PK=:uuid", nativeQuery = true)
    void insertFileIfNotExists(@Param("uuidPk")String uuid, @Param("pathChar") String path, @Param("nameChar")String name, @Param("typeChar")String type, @Param("sizeFloat")float size, @Param("locationChar")String location, @Param("stateInt")int state, @Param("deleteStatusInt")int deleteStatus);
}
