package com.example.hay_mart.init;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.hay_mart.constant.RoleConstant;
import com.example.hay_mart.models.Akun;
import com.example.hay_mart.models.Kategori;
import com.example.hay_mart.models.Role;
import com.example.hay_mart.repositorys.AkunRepository;
import com.example.hay_mart.repositorys.KategoriRepository;
import com.example.hay_mart.repositorys.RoleRepository;

@Component
public class InitialDataLoader implements ApplicationRunner{
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AkunRepository akunRepository;

    @Autowired
    private KategoriRepository kategoriRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception{
        if (roleRepository.findAll().isEmpty()) {
            Role admin = new Role(null, "ADMIN");
            Role kasir = new Role(null, "KASIR");
            roleRepository.saveAll(List.of(admin,kasir));
        }

        if (akunRepository.findAll().isEmpty()) {
            Akun admin = Akun.builder()
                .akunId(null)
                .username("ADMIN")
                .password(passwordEncoder.encode("ADMIN"))
                .role(roleRepository.findRoleByRoleName(RoleConstant.ROLE_ADMIN))
                .build();
            akunRepository.save(admin);
        }

        if (kategoriRepository.findAll().isEmpty()) {
            Kategori makanan = new Kategori(null, "Makanan");
            Kategori minuman = new Kategori(null, "Minuman");
            Kategori kecantikan = new Kategori(null, "Kecantikan");
            Kategori aksesoris = new Kategori(null, "Aksesoris");
            Kategori pakaian = new Kategori(null, "Pakaian");
            Kategori elektronik = new Kategori(null, "Elektronik");
            Kategori kesehatan = new Kategori(null, "Kesehatan");
            kategoriRepository.saveAll(List.of(makanan,minuman,kecantikan,aksesoris,pakaian,elektronik,kesehatan));
        }
    }
}
