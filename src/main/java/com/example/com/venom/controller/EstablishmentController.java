package com.example.com.venom.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.repository.EstabilishmentRepository;


@RestController
public class EstablishmentController {
    @Autowired
    private EstabilishmentRepository estabilishmentRepository;

    //==================================    Ркгистрация заведения    ==================================
    @PostMapping("/establishment")
    public ResponseEntity<?> Register (@RequestBody EstablishmentEntity establishmentEntity){
        Optional<EstablishmentEntity> existing = estabilishmentRepository.findByNameAndAddress(
            establishmentEntity.getName(),
            establishmentEntity.getAddress()
        );
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Заведение с таким названием и адресом уже существует");
        }
        EstablishmentEntity savedEstablishment = estabilishmentRepository.save(establishmentEntity);
        return ResponseEntity.ok().body(savedEstablishment);
    }

    //==================================    Получение всех заведений    ==================================
    @GetMapping("/establishment")
    public ResponseEntity<?> GetAll(){
        return ResponseEntity.ok().body(estabilishmentRepository.findAll());
    }

    //==================================    Получение заведения по id    ==================================
    @GetMapping("/establishment/{id}")
    public ResponseEntity<?> FindById (@PathVariable("id") Long id){
        Optional<EstablishmentEntity> existing = estabilishmentRepository.findById(id);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }
        else{
            return ResponseEntity.badRequest().body("Заведения с таким id не существует");
        }
    }

    //==================================    Получение заведения по названию    ==================================
    @GetMapping("/establishment/name/{name}")
    public ResponseEntity<?> FindByName (@PathVariable("name") String name){
        Optional<EstablishmentEntity> existing = estabilishmentRepository.findByName(name);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }
        else{
            return ResponseEntity.badRequest().body("Заведения с таким названием не существует");
        }
    }

    //==================================    Обновление данных заведения    ==================================
    @PutMapping("/establishment/{id}")
    public ResponseEntity<?> UpdateById (@PathVariable("id") Long id, @RequestBody EstablishmentEntity establishmentEntity){
        Optional<EstablishmentEntity> existing = estabilishmentRepository.findById(id);
        if (existing.isPresent()) {
            EstablishmentEntity establishmentEntityToUpadte = existing.get();
            establishmentEntityToUpadte = establishmentEntity;
            estabilishmentRepository.save(establishmentEntityToUpadte);
            return ResponseEntity.ok().body("Данные успешно обновлены");
        }
        else{
            return ResponseEntity.badRequest().body("Заведение с таким id не найдено");
        }
    }

     //==================================   Удаление заведения по id    ==================================
     @DeleteMapping("/establishment/{id}")
     public ResponseEntity<?> DeleteById (@PathVariable("id") Long id){
         Optional<EstablishmentEntity> existing = estabilishmentRepository.findById(id);
         if (existing.isPresent()) {
                estabilishmentRepository.delete(existing.get());
             return ResponseEntity.ok().body("Ресторан успешно удален");
         }
         else{
             return ResponseEntity.badRequest().body("Заведение с таким id не найдено");
         }
     }
}