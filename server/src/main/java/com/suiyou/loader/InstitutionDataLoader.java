package com.suiyou.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suiyou.model.SysInstitution;
import com.suiyou.repository.SysInstitutionRepository;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Order(1)
@lombok.extern.slf4j.Slf4j
public class InstitutionDataLoader extends AbstractConfigLoader {

    @Autowired
    private SysInstitutionRepository institutionRepository;
    
    @Value("classpath:sys_institution_init.json")
    private Resource jsonResource;
    
    @Override
    protected void loadConfig() throws Exception {
        if (!jsonResource.exists()) {
            log.warn("金融机构配置文件不存在，跳过加载");
            return;
        }

        List<SysInstitution> institutions = objectMapper.readValue(
            jsonResource.getInputStream().readAllBytes(), 
            new TypeReference<List<SysInstitution>>() {}
        );
        
        List<SysInstitution> existingList = institutionRepository.findAll();
        Map<String, SysInstitution> existingMap = existingList.stream()
            .collect(Collectors.toMap(SysInstitution::getInstCode, inst -> inst));
        
        List<SysInstitution> toSave = new ArrayList<>();
        
        for (SysInstitution dto : institutions) {
            String indexLetter = null;
            if (dto.getShortName() != null && !dto.getShortName().trim().isEmpty()) {
                indexLetter = getChineseInitialLetters(dto.getShortName());
            }
            
            SysInstitution existing = existingMap.get(dto.getInstCode());
            if (existing != null) {
                existing.setInstName(dto.getInstName());
                existing.setShortName(dto.getShortName());
                existing.setInstType(dto.getInstType());
                existing.setLogoUrl(dto.getLogoUrl());
                existing.setSortOrder(dto.getSortOrder());
                existing.setIsHot(dto.getIsHot() != null ? dto.getIsHot() : false);
                existing.setIndexLetter(indexLetter);
                toSave.add(existing);
            } else {
                dto.setIndexLetter(indexLetter);
                toSave.add(dto);
            }
        }
        
        institutionRepository.saveAll(toSave);
        
        updateConfigVersion("institution_data", DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(institutions)));
        
        log.info("金融机构数据同步完成，共 {} 条记录", toSave.size());
    }
    
    @Override
    protected String getLoaderName() {
        return "金融机构数据加载器";
    }
    
    private String getChineseInitialLetters(String chineseStr) {
        if (chineseStr == null || chineseStr.trim().isEmpty()) {
            return "#";
        }
        
        try {
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(net.sourceforge.pinyin4j.format.HanyuPinyinCaseType.UPPERCASE);
            format.setToneType(net.sourceforge.pinyin4j.format.HanyuPinyinToneType.WITHOUT_TONE);
            
            char[] chars = chineseStr.toCharArray();
            
            for (char c : chars) {
                if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        return String.valueOf(pinyinArray[0].charAt(0));
                    }
                } else if (Character.isLetter(c)) {
                    return String.valueOf(Character.toUpperCase(c));
                }
            }
            
            return "#";
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            log.warn("拼音转换失败: {}, 错误: {}", chineseStr, e.getMessage());
            return "#";
        }
    }
}