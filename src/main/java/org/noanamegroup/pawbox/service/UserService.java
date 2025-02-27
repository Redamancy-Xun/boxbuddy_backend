package org.noanamegroup.pawbox.service;

import java.time.LocalDateTime;
import java.util.List;

import org.noanamegroup.pawbox.dao.BoxDAO;
import org.noanamegroup.pawbox.dao.UserDAO;
import org.noanamegroup.pawbox.entity.Box;
import org.noanamegroup.pawbox.entity.User;
import org.noanamegroup.pawbox.entity.dto.BoxDTO;
import org.noanamegroup.pawbox.entity.dto.UserDTO;
import org.noanamegroup.pawbox.exception.UserNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service
public class UserService implements UserServiceImpl
{
    private final UserDAO userDAO;
    private final BoxDAO boxDAO;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserDAO userDAO, BoxDAO boxDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.boxDAO = boxDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(UserDTO userDTO)
    {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDAO.insert(user);
        return user;
    }

    @Override
    public User getUser(Integer userId) {
        User user = userDAO.selectById(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在: " + userId);
        }
        return user;
    }

    @Override
    public User updateUser(UserDTO userDTO) {
        User user = userDAO.selectById(userDTO.getUserId());
        if (user == null) {
            return null;
        }
        BeanUtils.copyProperties(userDTO, user);
        userDAO.updateById(user);
        return user;
    }

    @Override
    public User deleteUser(Integer userId)
    {
        User user = userDAO.selectById(userId);
        if (user == null) {
            return null;
        }
        userDAO.deleteById(userId);
        return user;
    }

    @Override
    public User login(String email, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = userDAO.selectOne(queryWrapper);
        
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public Box sendBox(BoxDTO boxDTO, Integer senderId) {
        Box box = new Box();
        box.setContent(boxDTO.getContent());
        box.setCreateTime(LocalDateTime.now());
        box.setImageUrl(boxDTO.getImageUrl());  // 直接设置图片URL

        box.setSender(userDAO.selectById(senderId));
        boxDAO.insert(box);
        return box;
    }

    @Override
    public Box receiveBox(Integer boxId, Integer receiverId) {
        Box box = boxDAO.selectById(boxId);
        if (box == null) {
            return null;
        }
        User receiver = userDAO.selectById(receiverId);
        if (receiver == null) {
            return null;
        }
        List<User> receivers = box.getReceivers();
        receivers.add(receiver);
        box.setReceivers(receivers);
        boxDAO.updateById(box);
        return box;
    }

    @Override
    public User findByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return userDAO.selectOne(queryWrapper);
    }
}