package com.suiyou.controller;

import com.suiyou.dto.account.AccountModuleDTO;
import com.suiyou.dto.account.AccountModuleRespDTO;
import com.suiyou.dto.account.BatchUpdateAccountsDTO;
import com.suiyou.dto.account.CreateAccountDTO;
import com.suiyou.dto.account.UpdateAccountDTO;
import com.suiyou.dto.account.AccountRespDTO;
import com.suiyou.model.Account;
import com.suiyou.service.AccountModuleService;
import com.suiyou.service.AccountService;
import com.suiyou.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountModuleService accountModuleService;

    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountDTO createAccountDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Account created = accountService.createAccount(createAccountDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", created.getId(),
                "message", "账户添加成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "账户添加失败",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAccounts(@RequestParam(required = false) String instCode) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            List<AccountRespDTO> accounts;
            if (instCode != null && !instCode.isEmpty()) {
                accounts = accountService.getAccountsByUserIdAndInstCode(userId, instCode);
            } else {
                accounts = accountService.getAccountsByUserId(userId);
            }
            return ResponseEntity.ok(Map.of(
                "accounts", accounts,
                "count", accounts.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "获取资产账户失败",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable Long accountId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            AccountRespDTO accountRespDTO = accountService.getAccountById(accountId);
            if (accountRespDTO.getOwnerId() != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "您没有权限访问此资产账户"
                ));
            }
            return ResponseEntity.ok(Map.of(
                "account", accountRespDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "获取资产账户失败",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateAccount(@RequestBody UpdateAccountDTO updateAccountDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            AccountRespDTO updatedAccount = accountService.updateAccount(updateAccountDTO, userId);
            return ResponseEntity.ok(Map.of(
                "id", updatedAccount.getId(),
                "message", "账户修改成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "账户修改失败",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAccountStatus(@PathVariable Long id, @RequestBody Map<String, Integer> statusRequest) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Integer status = statusRequest.get("status");
            if (status == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "参数错误",
                    "message", "缺少状态参数"
                ));
            }
            accountService.updateAccountStatus(id, status, userId);
            String statusText = status == 1 ? "恢复" : "归档";
            return ResponseEntity.ok(Map.of(
                "id", id,
                "message", "账户已" + statusText
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "更新账户状态失败",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            accountService.deleteAccount(id, userId);
            return ResponseEntity.ok(Map.of(
                "id", id,
                "message", "账户删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "账户删除失败",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/sync")
    public ResponseEntity<?> batchUpdateAccounts(@Valid @RequestBody BatchUpdateAccountsDTO batchUpdateDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        try {
            accountService.batchUpdateAccounts(userId, batchUpdateDTO.getActiveAccountIds(), batchUpdateDTO.getArchivedAccountIds());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "账户配置已生效"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "账户配置失败：" + e.getMessage()
            ));
        }
    }

    @PostMapping("/{accountId}/modules")
    public ResponseEntity<?> addAccountModule(@PathVariable Long accountId, @Valid @RequestBody AccountModuleDTO moduleDTO) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            accountModuleService.addModule(accountId, moduleDTO.getAssetType(), moduleDTO.getModuleName(), userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "资产模块添加成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{accountId}/modules/{moduleId}")
    public ResponseEntity<?> removeAccountModule(@PathVariable Long accountId, @PathVariable Long moduleId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            accountModuleService.removeModule(accountId, moduleId, userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "资产模块删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{accountId}/modules")
    public ResponseEntity<?> getAccountModules(@PathVariable Long accountId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            List<AccountModuleRespDTO> modules = accountModuleService.getModulesByAccountId(accountId, userId);
            return ResponseEntity.ok(Map.of(
                "modules", modules,
                "count", modules.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
