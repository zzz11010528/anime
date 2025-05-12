package com.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.entity.Ip;
import com.community.mapper.IpMapper;
import com.community.service.IpService;
import org.springframework.stereotype.Service;

/**
 * IP服务实现类
 */
@Service
public class IpServiceImpl extends ServiceImpl<IpMapper, Ip> implements IpService {
}
