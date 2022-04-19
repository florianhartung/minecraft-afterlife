package controller;

import data.TokenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.TokenRepository;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TokenController {
    @Autowired
    private TokenRepository tokenRepository;

    @GetMapping("/new/{uuid}")
    public ResponseEntity<TokenEntity> newToken(@PathVariable("uuid") String uuid) {
        return ResponseEntity.ok(
                tokenRepository.findByPlayerUUID(uuid)
                        .orElseGet(() -> tokenRepository.save(generateNewTokenForPlayer(uuid))));
    }

    @GetMapping("/{token}")
    public ResponseEntity<TokenEntity> getToken(@PathVariable("token") String token) {
        return ResponseEntity.of(tokenRepository.findById(token));
    }

    @GetMapping("/revoke/{uuid}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void revokeToken(@PathVariable("uuid") String uuid) {
        tokenRepository.deleteByPlayerUUID(uuid);
    }


    private TokenEntity generateNewTokenForPlayer(@PathVariable("uuid") String playerUUID) {
        TokenEntity token = new TokenEntity();
        token.setPlayerUUID(playerUUID);
        return token;
    }
}
