package com.coolbank.service;

import com.coolbank.dto.UsersDTO;
import com.coolbank.model.Users;
import com.coolbank.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class UsersServiceImpl implements UsersService {
    private static final Logger logger = LoggerFactory.getLogger(UsersServiceImpl.class);
    private final UsersRepository usersRepository;

    @Autowired
    public UsersServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }


    private UsersDTO convertUsersModelToDTO(Users user) {
        UsersDTO usersDTO = new UsersDTO();
        usersDTO.setId(user.getId());
        usersDTO.setFullName(user.getFullName());
        usersDTO.setEmail(user.getEmail());
        usersDTO.setPhoneNumber(user.getPhoneNumber());
        usersDTO.setPassword(user.getPassword());
        return usersDTO;
    }

    private Users convertUsersDTOToModel(UsersDTO usersDTO) {
        Users users = new Users();
        users.setFullName(usersDTO.getFullName());
        users.setEmail(usersDTO.getEmail());
        users.setPassword(usersDTO.getPassword());
        users.setPhoneNumber(usersDTO.getPhoneNumber());
        users.setId(usersDTO.getId());
        users.setCreatedDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        users.setStatus("ACTIVE");
        return users;
    }

    @Override
    public UsersDTO createUser(UsersDTO usersDTO) {
        logger.info("Attempting to find User with Email: {}", usersDTO.getEmail());
        usersRepository.findByEmail(usersDTO.getEmail())
                .ifPresent(EntityUser -> {
                    logger.error("User with Email: {}, already exists", usersDTO.getEmail());
                    throw new ResponseStatusException(
                            HttpStatus.FOUND, "User with such Email ALREADY EXIST: " + usersDTO.getEmail());
                });
        Users user = usersRepository.save(convertUsersDTOToModel(usersDTO));
        logger.info("User created successfully: {}", usersDTO);
        return convertUsersModelToDTO(user);
    }

    @Override
    public UsersDTO getUserById(UUID userId) {
        logger.info("Attempting to find user with ID: {}", userId);
        return usersRepository.findById(userId)
                .map(this::convertUsersModelToDTO)
                .orElseThrow(() -> {
                    logger.error("User with ID: {} not found", userId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User with such ID was NOT Found: " + userId);
                });
    }

    @Override
    public UsersDTO getUserByEmail(String userEmail) {
        return usersRepository.findByEmail(userEmail)
                .map(this::convertUsersModelToDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with such Email was NOT Found" + userEmail));
    }

    @Override
    public UsersDTO getUserByFullName(String userFullName) {
        return usersRepository.findByFullName(userFullName)
                .map(this::convertUsersModelToDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with such FullName was NOT Found" + userFullName));
    }

    @Override
    public UsersDTO getUserByPhoneNumber(String userPhoneNumber) {
        return usersRepository.findByPhoneNumber(userPhoneNumber)
                .map(this::convertUsersModelToDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with such PhoneNumber was NOT Found" + userPhoneNumber));
    }

    @Override
    public UsersDTO updateUser(UUID userId, UsersDTO usersDTO) {
        return usersRepository.findById(usersDTO.getId())
                .map(EntityUser -> {
                    EntityUser.setFullName(usersDTO.getFullName());
                    EntityUser.setEmail(usersDTO.getEmail());
                    EntityUser.setPhoneNumber(usersDTO.getPhoneNumber());
                    EntityUser.setPassword(usersDTO.getPassword());
                    usersRepository.save(EntityUser);
                    return convertUsersModelToDTO(EntityUser);
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with such ID was NOT Found" + usersDTO.getId()));
    }

    @Override
    public UsersDTO updatePasswordById(UUID userId, String newPassword) {
        return usersRepository.findById(userId)
                .map(EntityUser -> {
                    EntityUser.setPassword(newPassword);
                    usersRepository.save(EntityUser);
                    return convertUsersModelToDTO(EntityUser);
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with such ID was NOT Found" + userId));
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteUserById(UUID userId) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with such ID was NOT Found" + userId));
        usersRepository.deleteById(userId);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.ACCEPTED);
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteUserByEmail(String userEmail) {
        usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with such Email was NOT Found" + userEmail));
        usersRepository.deleteByEmail(userEmail);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.ACCEPTED);
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteUserByFullName(String userFullName) {
        usersRepository.findByFullName(userFullName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with such Full Name was NOT Found" + userFullName));
        usersRepository.deleteByFullName(userFullName);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.ACCEPTED);
    }
}