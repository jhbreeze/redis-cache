package com.example.redis;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.redis.domain.Item;
import com.example.redis.domain.ItemDto;
import com.example.redis.repo.ItemRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ItemService {
    private final ItemRepository itemRepository;
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /*
     @CachePut
     : Write-Through 전략.
     : 항상 메서드를 실행하고, 결과를 캐싱함.
     : 반환되는 id가 캐싱되기 때문에 readOne 메서드에서도 그 결과를 활용할 수 있음.
     */
    @CachePut(cacheNames = "itemCache", key = "#result.id")
    public ItemDto create(ItemDto dto) {
        return ItemDto.fromEntity(itemRepository.save(Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .build()));
    }

    @Cacheable(cacheNames = "itemAllCache", key = "methodName")
    public List<ItemDto> readAll() {
        return itemRepository.findAll()
                .stream()
                .map(ItemDto::fromEntity)
                .toList();
    }

    /*
     @Cacheable
     : Cache Aside 방식.
     : 조회가 잦은 경우 사용
     */
    @Cacheable(cacheNames = "itemCache", key = "args[0]")
    public ItemDto readOne(Long id) {
        log.info("Read One: {}", id);
        return itemRepository.findById(id)
                .map(ItemDto::fromEntity)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /*
     @CacheEvict(allEntries = true)
     : readAll에서 저장된 캐시를 지우기 위해 사용
     : 아이템의 정보가 바뀌었으니, 데이터를 전부 돌려준 결과가 더이상 유효하지 않다라는 의미
     */
    @CachePut(cacheNames = "itemCache", key = "args[0]")
    @CacheEvict(cacheNames = "itemAllCache", allEntries = true)
    public ItemDto update(Long id, ItemDto dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        return ItemDto.fromEntity(itemRepository.save(item));
    }

    public void delete(Long id) {
        itemRepository.deleteById(id);
    }

    @Cacheable(cacheNames = "itemSearchCache",
        key = "{ args[0], args[1].pageNumber, args[1].pageSize }")
    public Page<ItemDto> searchByName(String query, Pageable pageable) {
        return itemRepository.findAllByNameContains(query, pageable)
            .map(ItemDto::fromEntity);
    }

}
