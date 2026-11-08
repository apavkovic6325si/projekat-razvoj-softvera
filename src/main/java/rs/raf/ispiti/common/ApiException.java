package rs.raf.ispiti.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String poruka) {
        super(poruka);
        this.status = status;
    }

    public static ApiException nijePronadjeno(String poruka) {
        return new ApiException(HttpStatus.NOT_FOUND, poruka);
    }

    public static ApiException losZahtev(String poruka) {
        return new ApiException(HttpStatus.BAD_REQUEST, poruka);
    }

    public static ApiException zabranjeno(String poruka) {
        return new ApiException(HttpStatus.FORBIDDEN, poruka);
    }

    public static ApiException konflikt(String poruka) {
        return new ApiException(HttpStatus.CONFLICT, poruka);
    }
}
