meta:
  id: mesh
  endian: be

seq:
  - size: 4
  - id: vertex_count
    type: u2
  - id: face_count
    type: u2
  - size: 0x38
  - id: vertices
    type: vertex
    repeat: expr
    repeat-expr: vertex_count
  - type: padding
  - id: normals
    size: vertex_count * 12
  - type: padding
  - id: uvs
    type: uv
    repeat: expr
    repeat-expr: vertex_count
  - type: padding
  - id: faces
    type: face
    repeat: expr
    repeat-expr: face_count

types:
  face:
    seq:
      - id: v1
        type: u2
      - id: v2
        type: u2
      - id: v3
        type: u2
  vertex:
    seq:
      - id: x
        type: f4
      - id: y
        type: f4
      - id: z
        type: f4
  normal:
    seq:
      # TODO: tried (U)DEC3, but didn't look like normalized vectors
      # other way of packing? angles instead?
      - id: packed_normal #?
        size: 4
      - id: packed_tangent #?
        size: 4
      - id: packed_bitangent #?
        size: 4
  uv:
    seq:
      - id: u
        type: f2
      - id: v
        type: f2
  f2:
    seq:
      - id: sign
        type: b1
      - id: exponent
        type: b5
      - id: fraction
        type: b10
    instances:
      value:
        # exponent == 0
        #   ? pow(-1, sign) * pow(2, -14) * 0.significantbits
        #   : pow(-1, sign) * pow(2, exponent - 15) * 1.significantbits
        # note :NaN not implemented # TODO: investigate 16-bit SNORM and UNORM
        value: >
          exponent == 0
          ?
            ((sign ? -1 : 1) *
            (1.0 / (1 << 14)) *
            (0 + fraction / 1024.0))
          :
            ((sign ? -1 : 1) *
            (exponent >= 15 ? 1 << (exponent - 15) : 1.0 / (1 << (-1 * (exponent - 15)))) *
            (1 + fraction / 1024.0))
  padding:
    seq:
      - size: (0x10 - _io.pos) % 0x10
