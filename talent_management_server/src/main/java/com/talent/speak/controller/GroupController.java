package com.talent.speak.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.StringUtils;
import com.talent.speak.entity.ChatGroup;
import com.talent.speak.entity.ChatGroupRead;
import com.talent.speak.entity.ChatMessage;
import com.talent.speak.mapper.ChatGroupMapper;
import com.talent.speak.mapper.ChatGroupReadMapper;
import com.talent.speak.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 群管理 Controller（管理员用）
 */
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final ChatGroupMapper groupMapper;

    private final ChatGroupReadMapper chatGroupReadMapper;

    private final ChatMessageMapper messageMapper;

    /**
     * 获取所有群列表
     */
    @GetMapping("/selectGroup")
    public AjaxResult listGroups() {
        Long userId = SecurityUtils.getLoginUser().getUserId();

        // 只取与自己有关的群组
        List<ChatGroup> groups = groupMapper.selectList(
                new LambdaQueryWrapper<ChatGroup>()
                        .like(ChatGroup::getUserIds, userId)  // SQL: user_ids like '%123%'
        );

        for (ChatGroup g : groups) {

            // 取我在该群的最后已读时间
            ChatGroupRead read = chatGroupReadMapper.selectOne(new LambdaQueryWrapper<ChatGroupRead>()
                    .eq(ChatGroupRead::getGroupId, g.getId())
                    .eq(ChatGroupRead::getUserId, userId)
                    .last("limit 1"));

            Date lastReadTime = (read != null) ? read.getLastReadTime() : new Date(0);

            // 统计未读：send_time > lastReadTime  等于大于send_time>lastReadTime的数量，就是未读数量
            Integer unread = messageMapper.selectCount(
                    new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getGroupId, String.valueOf(g.getId()))
                            .gt(ChatMessage::getSendTime, lastReadTime)
            );
            g.setUnreadCount(unread);
        }

        // 👇 按未读数量倒序排，如果未读相同按创建时间或id排
        groups.sort(Comparator
                .comparing(ChatGroup::getUnreadCount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ChatGroup::getId)); // 需要二次排序可加

        return AjaxResult.success(groups);
    }

    /**
     * 创建群
     */
    @PostMapping("/addGroup")
    public AjaxResult create(@RequestBody ChatGroup group){
        group.setId(null);
        group.setCreateTime(new Date());
        // 新群默认成员是自己 和 199
        group.setUserIds(String.valueOf(SecurityUtils.getLoginUser().getUserId()) + ",199");
        groupMapper.insert(group);
        return AjaxResult.success(group);
    }

    /**
     * 修改群
     */
    @PostMapping("/updateName")
    public AjaxResult updateName(@RequestBody ChatGroup group) {
        if (group.getId() == null) return AjaxResult.error("参数不能为空");
        ChatGroup old = groupMapper.selectById(group.getId());
        if (old == null) return AjaxResult.error("群不存在");
        old.setName(group.getName());
        groupMapper.updateById(old);
        return AjaxResult.success(old);
    }

    /**
     * 解散群
     */
    @DeleteMapping("/delGroup/{id}")
    public AjaxResult delete(@PathVariable Long id){
        groupMapper.deleteById(id);
        return AjaxResult.success();
    }

    /**
     * 添加成员
     */
    @PostMapping("/addMember")
    public AjaxResult addMember(@RequestBody ChatGroup group){
        // 必须有群ID
        if (group.getId() == null) return AjaxResult.error("参数不能为空");

        ChatGroup old = groupMapper.selectById(group.getId());
        if (old == null) return AjaxResult.error("参数不能为空");;

        // 解析原有 userIds
        Set<String> set = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(old.getUserIds())) {
            set.addAll(Arrays.asList(old.getUserIds().split(",")));
        }

        // 追加新成员
        if (group.getUserIdParam() != null) {
            group.getUserIdParam().forEach(u -> set.add(String.valueOf(u)));
        }

        // 更新 userIds
        old.setUserIds(set.isEmpty() ? null : String.join(",", set));
        groupMapper.updateById(old);

        return AjaxResult.success(old);
    }

    /**
     * 删除成员
     */
    @DeleteMapping("/delMember")
    public AjaxResult removeMember(@RequestBody ChatGroup group){
        if (group.getId() == null) return AjaxResult.error("参数不能为空");

        ChatGroup old = groupMapper.selectById(group.getId());
        if (old == null) return AjaxResult.error("参数不能为空");;

        // 原有成员
        Set<String> set = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(old.getUserIds())) {
            set.addAll(Arrays.asList(old.getUserIds().split(",")));
        }

        // 移除传入的成员
        if (group.getUserIdParam() != null) {
            group.getUserIdParam().forEach(u -> set.remove(String.valueOf(u)));
        }

        // 更新 userIds
        old.setUserIds(set.isEmpty() ? null : String.join(",", set));
        groupMapper.updateById(old);

        return AjaxResult.success(old);
    }

    /**
     * 查询群历史消息
     */
    @GetMapping("/history/{groupId}")
    public AjaxResult history(@PathVariable Long groupId) {
        List<ChatMessage> list = messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getGroupId, groupId)
                        .orderByAsc(ChatMessage::getSendTime)
        );
        return AjaxResult.success(list);
    }

    /**
     * 进入群后“标记为已读”
     */
    @PostMapping("/{groupId}/read")
    public AjaxResult markRead(@PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUser().getUserId();

        // 取该群最后一条消息的时间（如果没有消息，就用当前时间）
        ChatMessage last = messageMapper.selectOne(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getGroupId, String.valueOf(groupId))
                        .orderByDesc(ChatMessage::getId)
                        .last("limit 1")
        );
        Date lastTime = (last != null) ? last.getSendTime() : new Date();

        // upsert
        ChatGroupRead exist = chatGroupReadMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupRead>()
                        .eq(ChatGroupRead::getGroupId, groupId)
                        .eq(ChatGroupRead::getUserId, userId)
        );

        if (exist == null) {
            ChatGroupRead r = new ChatGroupRead();
            r.setGroupId(groupId);
            r.setUserId(userId);
            r.setLastReadTime(lastTime);
            chatGroupReadMapper.insert(r);
        } else {
            exist.setLastReadTime(lastTime);
            chatGroupReadMapper.updateById(exist);
        }
        return AjaxResult.success();
    }
}
