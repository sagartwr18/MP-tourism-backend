package com.mptourism.repository;

import com.mptourism.model.LocationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationDetailRepository extends JpaRepository<LocationDetail, Integer> {
    List<LocationDetail> findByCategoryId(int categoryId);
    Optional<LocationDetail> findByLocationIdAndCategoryId(int locationId, int categoryId);
}
